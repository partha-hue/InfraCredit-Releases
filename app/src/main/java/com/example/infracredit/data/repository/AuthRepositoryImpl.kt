package com.example.infracredit.data.repository

import com.example.infracredit.data.local.TokenManager
import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.*
import com.example.infracredit.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: InfracreditApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = api.login(request)
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = api.register(request)
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(): Result<ProfileDto> {
        return try {
            val response = api.getProfile()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(profile: ProfileDto): Result<ProfileDto> {
        return try {
            val response = api.updateProfile(profile)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenManager.accessToken.firstOrNull() != null
    }
}