package com.example.infracredit.data.remote.dto

import com.example.infracredit.domain.model.Customer
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    val id: String? = null,
    val name: String,
    val phone: String?,
    val totalDue: Double = 0.0,
    val isDeleted: Boolean = false,
    val createdAt: String? = null
)

fun CustomerDto.toDomain() = Customer(
    id = id ?: "",
    name = name,
    phone = phone,
    totalDue = totalDue,
    createdAt = createdAt ?: ""
)

fun Customer.toDto() = CustomerDto(
    id = id,
    name = name,
    phone = phone,
    totalDue = totalDue,
    createdAt = createdAt
)
