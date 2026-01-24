package com.example.infracredit.domain.model

data class Customer(
    val id: String,
    val name: String,
    val phone: String?,
    val totalDue: Double,
    val createdAt: Long
)