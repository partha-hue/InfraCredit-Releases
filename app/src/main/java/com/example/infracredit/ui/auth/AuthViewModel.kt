package com.example.infracredit.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.data.remote.dto.LoginRequest
import com.example.infracredit.data.remote.dto.RegisterRequest
import com.example.infracredit.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(AuthState())
    val state: State<AuthState> = _state

    fun login(phone: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            repository.login(LoginRequest(phone, pass))
                .onSuccess {
                    _state.value = AuthState(isAuthenticated = true)
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("401") == true -> "Incorrect phone or password. Please try again."
                        e.message?.contains("404") == true -> "Account not found. Please register."
                        else -> "Connection error. Please check your internet."
                    }
                    _state.value = AuthState(error = msg)
                }
        }
    }

    fun register(fullName: String, businessName: String?, phone: String, pass: String, email: String? = null) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            repository.register(RegisterRequest(
                fullName = fullName, 
                businessName = businessName, 
                phone = phone, 
                password = pass,
                email = email
            ))
                .onSuccess {
                    _state.value = AuthState(isAuthenticated = true)
                }
                .onFailure { e ->
                    _state.value = AuthState(error = "Registration failed. Phone might already be in use.")
                }
        }
    }
}
