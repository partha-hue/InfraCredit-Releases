package com.example.infracredit.ui.settings

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.infracredit.data.local.PreferenceManager
import com.example.infracredit.data.local.TokenManager
import com.example.infracredit.data.remote.dto.ProfileDto
import com.example.infracredit.data.repository.BackupRepository
import com.example.infracredit.data.worker.BackupWorker
import com.example.infracredit.domain.repository.AuthRepository
import com.example.infracredit.ui.theme.ThemeManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: ProfileDto? = null,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false
)

data class PasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val preferenceManager: PreferenceManager,
    private val backupRepository: BackupRepository,
    private val themeManager: ThemeManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val WEB_CLIENT_ID = "647476966614-b0m1qn7j9uuqvciv4gif9kcpoau1ja8c.apps.googleusercontent.com"

    private val _profileState = mutableStateOf(ProfileState())
    val profileState: State<ProfileState> = _profileState

    private val _passwordState = mutableStateOf(PasswordState())
    val passwordState: State<PasswordState> = _passwordState

    val isDarkMode = themeManager.isDarkMode

    val lastBackupTime: StateFlow<Long> = preferenceManager.lastBackupTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _isBackingUp = mutableStateOf(false)
    val isBackingUp: State<Boolean> = _isBackingUp

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            authRepository.getProfile()
                .onSuccess { profile ->
                    _profileState.value = ProfileState(profile = profile)
                }
                .onFailure { e ->
                    _profileState.value = ProfileState(error = e.message)
                }
        }
    }

    fun updateProfile(fullName: String, businessName: String?, profilePic: String?) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            authRepository.updateProfile(ProfileDto(
                fullName = fullName,
                businessName = businessName,
                profilePic = profilePic,
                id = _profileState.value.profile?.id ?: "",
                phone = _profileState.value.profile?.phone
            ))
                .onSuccess {
                    _profileState.value = _profileState.value.copy(isLoading = false, isUpdateSuccess = true)
                    loadProfile()
                }
                .onFailure { e ->
                    _profileState.value = _profileState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            _passwordState.value = PasswordState(isLoading = true)
            authRepository.resetPassword(oldPass, newPass)
                .onSuccess {
                    _passwordState.value = PasswordState(isSuccess = true)
                }
                .onFailure { e ->
                    _passwordState.value = PasswordState(error = e.message)
                }
        }
    }

    fun forgotPassword(phone: String) {
        viewModelScope.launch {
            _passwordState.value = PasswordState(isLoading = true)
            authRepository.forgotPassword(phone)
                .onSuccess { msg ->
                    _passwordState.value = PasswordState(isOtpSent = true, message = msg)
                }
                .onFailure { e ->
                    _passwordState.value = PasswordState(error = e.message)
                }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _passwordState.value = _passwordState.value.copy(isLoading = true, error = null)
            authRepository.verifyOtp(phone, otp)
                .onSuccess {
                    _passwordState.value = _passwordState.value.copy(
                        isLoading = false,
                        isOtpVerified = true,
                        error = null
                    )
                }
                .onFailure { e ->
                    _passwordState.value = _passwordState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Invalid OTP"
                    )
                }
        }
    }

    fun resetPasswordWithOtp(newPass: String) {
        viewModelScope.launch {
            _passwordState.value = _passwordState.value.copy(isLoading = true)
            authRepository.resetPassword(null, newPass)
                .onSuccess {
                    _passwordState.value = PasswordState(isSuccess = true, message = "Password Reset Successful")
                }
                .onFailure { e ->
                    _passwordState.value = _passwordState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun clearPasswordState() {
        _passwordState.value = PasswordState()
    }

    fun toggleDarkMode() {
        themeManager.toggleTheme()
    }

    fun backupNow(context: Context) {
        viewModelScope.launch {
            val account = preferenceManager.googleAccountName.first()
            if (account.isNullOrBlank()) {
                signInAndBackup(context)
            } else {
                _isBackingUp.value = true
                
                // For the very first backup (or when permission is needed), we call the repository directly 
                // because WorkManager cannot show the "UserRecoverableAuthIOException" dialog.
                val result = backupRepository.uploadBackup(account)
                
                if (result.isFailure) {
                    val exception = result.exceptionOrNull()
                    if (exception is UserRecoverableAuthIOException) {
                        // This handles the "Permission Required" case by opening the Google Consent screen
                        (context as? Activity)?.startActivityForResult(exception.intent, 1001)
                        _isBackingUp.value = false
                    } else {
                        // Fallback to Worker if it's a normal error or if direct upload failed for other reasons
                        startBackupWorker(context)
                    }
                } else {
                    _isBackingUp.value = false
                    preferenceManager.saveLastBackupTime(System.currentTimeMillis())
                    Toast.makeText(context, "Backup Successful", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startBackupWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<BackupWorker>().build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(workRequest)
        
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                _isBackingUp.value = false
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Toast.makeText(context, "Backup Successful", Toast.LENGTH_SHORT).show()
                } else {
                    val error = workInfo.outputData.getString("error") ?: "Backup Failed"
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun signInAndBackup(context: Context) {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is GoogleIdTokenCredential) {
                val email = credential.id
                if (email.isNotBlank()) {
                    preferenceManager.saveGoogleAccountName(email)
                    Toast.makeText(context, "Account Linked: $email", Toast.LENGTH_SHORT).show()
                    backupNow(context)
                } else {
                    _isBackingUp.value = false
                    Toast.makeText(context, "Failed to get account name", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            _isBackingUp.value = false
            Toast.makeText(context, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun restoreNow() {
        viewModelScope.launch {
            val account = preferenceManager.googleAccountName.first()
            if (!account.isNullOrBlank()) {
                _isBackingUp.value = true
                val result = backupRepository.restoreBackup(account)
                _isBackingUp.value = false
                
                result.onSuccess {
                    Toast.makeText(context, "Restore Successful. Please restart the app.", Toast.LENGTH_LONG).show()
                }.onFailure { e ->
                    if (e is UserRecoverableAuthIOException) {
                        (context as? Activity)?.startActivityForResult(e.intent, 1002)
                    } else {
                        Toast.makeText(context, "Restore Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please sign in first to restore", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            tokenManager.clearTokens()
            onSuccess()
        }
    }
}
