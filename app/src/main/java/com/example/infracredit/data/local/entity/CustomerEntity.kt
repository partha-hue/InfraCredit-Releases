package com.example.infracredit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.infracredit.domain.model.Customer

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String?,
    val totalDue: Double,
    val createdAt: String,
    val isSynced: Boolean = true
)

fun CustomerEntity.toDomain() = Customer(
    id = id,
    name = name,
    phone = phone,
    totalDue = totalDue,
    createdAt = createdAt
)

fun Customer.toEntity(isSynced: Boolean = true) = CustomerEntity(
    id = id,
    name = name,
    phone = phone,
    totalDue = totalDue,
    createdAt = createdAt,
    isSynced = isSynced
)
