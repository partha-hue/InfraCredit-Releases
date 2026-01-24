package com.example.infracredit.domain.repository

import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType

interface TransactionRepository {
    suspend fun getTransactions(customerId: String): Result<List<Transaction>>
    suspend fun addTransaction(
        customerId: String,
        amount: Double,
        type: TransactionType,
        description: String?
    ): Result<Transaction>
}