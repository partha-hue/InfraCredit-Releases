package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String? = null,
    val customerId: String? = null,
    val amount: Double? = null,
    val type: String? = null,
    val description: String? = null,
    val createdAt: String? = null
)
