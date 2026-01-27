package com.example.infracredit.domain.repository

import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsFlow(customerId: String): Flow<List<Transaction>>
    suspend fun getTransactions(customerId: String): Result<List<Transaction>>
    suspend fun addTransaction(
        customerId: String,
        amount: Double,
        type: TransactionType,
        description: String?
    ): Result<Transaction>
    suspend fun updateTransaction(
        id: String,
        amount: Double,
        type: TransactionType,
        description: String?
    ): Result<Transaction>
    suspend fun deleteTransaction(id: String): Result<Unit>
}
