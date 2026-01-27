package com.example.infracredit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val amount: Double,
    val type: TransactionType,
    val description: String?,
    val createdAt: String,
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

fun Transaction.toEntity(isSynced: Boolean = true) = TransactionEntity(
    id = id,
    customerId = customerId,
    amount = amount,
    type = type,
    description = description,
    createdAt = createdAt,
    isSynced = isSynced
)
