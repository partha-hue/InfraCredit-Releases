package com.example.infracredit.ui.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.data.remote.dto.ProfileDto
import com.example.infracredit.domain.repository.AuthRepository
import com.example.infracredit.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _profileState = mutableStateOf(ProfileState())
    val profileState: State<ProfileState> = _profileState

    val isDarkMode: State<Boolean> = themeManager.isDarkMode

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
                .onFailure { error ->
                    _profileState.value = ProfileState(error = error.message)
                }
        }
    }

    fun toggleDarkMode() {
        themeManager.toggleTheme()
    }

    fun updateProfile(fullName: String, businessName: String?, profilePic: String? = null) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            authRepository.updateProfile(ProfileDto(fullName = fullName, businessName = businessName, profilePic = profilePic))
                .onSuccess { profile ->
                    _profileState.value = ProfileState(profile = profile, isUpdateSuccess = true)
                }
                .onFailure { error ->
                    _profileState.value = _profileState.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }
}

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: ProfileDto? = null,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false
)
