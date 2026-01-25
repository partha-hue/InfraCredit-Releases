package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val businessName: String? = null,
    val phone: String,
    val password: String,
    val email: String? = null // For migration
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val fullName: String,
    val businessName: String? = null
)

@Serializable
data class ProfileDto(
    val id: String,
    val phone: String?,
    val fullName: String,
    val businessName: String? = null,
    val profilePic: String? = null,
    val email: String? = null,
    val address: String? = null,
    val isMigrated: Boolean = false,
    val createdAt: String
)