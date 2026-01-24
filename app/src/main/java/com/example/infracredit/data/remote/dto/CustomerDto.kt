package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    val id: String? = null,
    val name: String,
    val phone: String?,
    val totalDue: Double = 0.0,
    val createdAt: Long? = null
)