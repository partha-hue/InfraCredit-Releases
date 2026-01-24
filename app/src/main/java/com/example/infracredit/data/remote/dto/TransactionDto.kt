package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String? = null,
    val customerId: String,
    val amount: Double,
    val type: String, // "CREDIT" or "PAYMENT"
    val description: String?,
    val createdAt: Long? = null
)