package com.example.infracredit.data.repository

import com.example.infracredit.data.local.dao.TransactionDao
import com.example.infracredit.data.local.entity.toDomain
import com.example.infracredit.data.local.entity.toEntity
import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.TransactionDto
import com.example.infracredit.data.remote.dto.toDomain
import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType
import com.example.infracredit.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: InfracreditApi,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getTransactionsFlow(customerId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForCustomer(customerId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getTransactions(customerId: String): Result<List<Transaction>> {
        return try {
            val response = api.getTransactions(customerId)
            val transactions = response.map { it.toDomain() }
            
            // Sync to local DB
            transactionDao.insertTransactions(transactions.map { it.toEntity() })
            
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addTransaction(customerId: String, amount: Double, type: TransactionType, description: String?): Result<Transaction> {
        return try {
            val response = api.addTransaction(
                TransactionDto(customerId = customerId, amount = amount, type = type.name, description = description)
            )
            val transaction = response.toDomain()
            transactionDao.insertTransaction(transaction.toEntity())
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(id: String, amount: Double, type: TransactionType, description: String?): Result<Transaction> {
        return try {
            val response = api.updateTransaction(
                id,
                TransactionDto(id = id, amount = amount, type = type.name, description = description)
            )
            val transaction = response.toDomain()
            transactionDao.insertTransaction(transaction.toEntity())
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            api.deleteTransaction(id)
            transactionDao.deleteTransaction(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
