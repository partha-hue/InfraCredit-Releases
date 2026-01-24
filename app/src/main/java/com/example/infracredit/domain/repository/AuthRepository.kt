package com.example.infracredit.domain.repository

import com.example.infracredit.data.remote.dto.AuthResponse
import com.example.infracredit.data.remote.dto.LoginRequest
import com.example.infracredit.data.remote.dto.ProfileDto
import com.example.infracredit.data.remote.dto.RegisterRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun getProfile(): Result<ProfileDto>
    suspend fun updateProfile(profile: ProfileDto): Result<ProfileDto>
    suspend fun logout()
    suspend fun isAuthenticated(): Boolean
}