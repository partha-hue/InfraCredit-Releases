package com.example.infracredit.domain.repository

import com.example.infracredit.data.remote.dto.*

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun getProfile(): Result<ProfileDto>
    suspend fun updateProfile(profile: ProfileDto): Result<ProfileDto>
    suspend fun logout()
    suspend fun isAuthenticated(): Boolean
    suspend fun forgotPassword(phone: String): Result<String>
    suspend fun verifyOtp(phone: String, otp: String): Result<String>
    suspend fun resetPassword(oldPass: String?, newPass: String): Result<String>
}
