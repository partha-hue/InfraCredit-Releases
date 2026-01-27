package com.example.infracredit.ui.settings

import android.content.Context
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
                    _passwordState.value = PasswordState(isSuccess = true, message = msg)
                }
                .onFailure { e ->
                    _passwordState.value = PasswordState(error = e.message)
                }
        }
    }

    fun toggleDarkMode() {
        themeManager.toggleTheme()
    }

    fun backupNow(context: Context) {
        viewModelScope.launch {
            val account = preferenceManager.googleAccountName.first()
            if (account == null) {
                signInAndBackup(context)
            } else {
                _isBackingUp.value = true
                val workRequest = OneTimeWorkRequestBuilder<BackupWorker>().build()
                val workManager = WorkManager.getInstance(context)
                workManager.enqueue(workRequest)
                
                // Observe work status for UI feedback
                workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
                    if (workInfo != null) {
                        if (workInfo.state.isFinished) {
                            _isBackingUp.value = false
                            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                Toast.makeText(context, "Backup Successful", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Backup Failed: No local data found", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
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
                preferenceManager.saveGoogleAccountName(email)
                Toast.makeText(context, "Account Linked: $email", Toast.LENGTH_SHORT).show()
                backupNow(context) // Retry backup now that we have the account
            }
        } catch (e: Exception) {
            _isBackingUp.value = false
            Toast.makeText(context, "Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun restoreNow() {
        viewModelScope.launch {
            val account = preferenceManager.googleAccountName.first()
            if (account != null) {
                _isBackingUp.value = true
                val result = backupRepository.restoreBackup(account)
                _isBackingUp.value = false
                if (result.isSuccess) {
                    Toast.makeText(context, "Restore Successful. Please restart the app.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Restore Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Please backup first to link account", Toast.LENGTH_SHORT).show()
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
