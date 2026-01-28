package com.example.infracredit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val ownerPhone: String, // Isolated by user's phone number
    val customerId: String,
    val amount: Double,
    val type: TransactionType,
    val description: String?,
    val createdAt: String,
    val isDeleted: Int = 0, // 0 for active, 1 for deleted
    val isSynced: Boolean = true
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    customerId = customerId,
    amount = amount,
    type = type,
    description = description,
    createdAt = createdAt
)

fun Transaction.toEntity(ownerPhone: String, isSynced: Boolean = true, isDeleted: Int = 0) = TransactionEntity(
    id = id,
    ownerPhone = ownerPhone,
    customerId = customerId,
    amount = amount,
    type = type,
    description = description,
    createdAt = createdAt,
    isDeleted = isDeleted,
    isSynced = isSynced
)
