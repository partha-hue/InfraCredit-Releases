package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val oldPassword: String? = null,
    val newPassword: String
)
