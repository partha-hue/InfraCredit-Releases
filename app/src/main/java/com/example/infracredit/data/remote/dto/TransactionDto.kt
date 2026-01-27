package com.example.infracredit.data.remote.dto

import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType
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

fun TransactionDto.toDomain() = Transaction(
    id = id ?: "",
    customerId = customerId ?: "",
    amount = amount ?: 0.0,
    type = try { TransactionType.valueOf(type ?: "CREDIT") } catch(e: Exception) { TransactionType.CREDIT },
    description = description,
    createdAt = createdAt ?: ""
)

fun Transaction.toDto() = TransactionDto(
    id = id,
    customerId = customerId,
    amount = amount,
    type = type.name,
    description = description,
    createdAt = createdAt
)
