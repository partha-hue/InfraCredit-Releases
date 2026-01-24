package com.example.infracredit.domain.model

enum class TransactionType {
    CREDIT, PAYMENT
}

data class Transaction(
    val id: String,
    val customerId: String,
    val amount: Double,
    val type: TransactionType,
    val description: String?,
    val createdAt: Long
)