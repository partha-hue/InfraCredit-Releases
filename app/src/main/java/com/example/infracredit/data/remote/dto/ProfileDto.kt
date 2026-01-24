package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String? = null,
    val phone: String? = null,
    val fullName: String,
    val businessName: String? = null,
    val createdAt: String? = null
)

@Serializable
data class GenericResponse(
    val success: Boolean,
    val message: String? = null
)