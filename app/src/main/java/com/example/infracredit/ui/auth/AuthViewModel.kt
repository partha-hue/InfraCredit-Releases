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
            val result = repository.login(LoginRequest(phone, pass))
            if (result.isSuccess) {
                _state.value = AuthState(isAuthenticated = true)
            } else {
                _state.value = AuthState(error = result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, business: String?, phone: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repository.register(RegisterRequest(name, business, phone, pass))
            if (result.isSuccess) {
                _state.value = AuthState(isAuthenticated = true)
            } else {
                _state.value = AuthState(error = result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}