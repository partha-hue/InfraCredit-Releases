package com.example.infracredit.domain.model

data class User(
    val id: String,
    val fullName: String,
    val businessName: String?,
    val phone: String
)