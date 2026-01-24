package com.example.infracredit.domain.model

data class Customer(
    val id: String,
    val name: String,
    val phone: String?,
    val totalDue: Double,
    val createdAt: String // Changed to String to match MongoDB ISO 8601
)