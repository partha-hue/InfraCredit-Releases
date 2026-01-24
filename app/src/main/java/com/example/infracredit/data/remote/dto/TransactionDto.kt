package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String? = null,
    val customerId: String,
    val amount: Double,
    val type: String,
    val description: String? = null,
    val createdAt: String? = null // Changed to String to handle ISO 8601 from MongoDB
)