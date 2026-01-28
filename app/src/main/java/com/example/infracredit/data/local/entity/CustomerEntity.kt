package com.example.infracredit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.infracredit.domain.model.Customer

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val ownerPhone: String, // Isolated by user's phone number
    val name: String,
    val phone: String?,
    val totalDue: Double,
    val createdAt: String,
    val isDeleted: Int = 0, // 0 for active, 1 for deleted (Recycle Bin)
    val isSynced: Boolean = true
)

fun CustomerEntity.toDomain() = Customer(
    id = id,
    name = name,
    phone = phone,
    totalDue = totalDue,
    createdAt = createdAt
)

fun Customer.toEntity(ownerPhone: String, isSynced: Boolean = true, isDeleted: Int = 0) = CustomerEntity(
    id = id,
    ownerPhone = ownerPhone,
    name = name,
    phone = phone,
    totalDue = totalDue,
    createdAt = createdAt,
    isDeleted = isDeleted,
    isSynced = isSynced
)
