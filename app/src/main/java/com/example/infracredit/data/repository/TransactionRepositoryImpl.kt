package com.example.infracredit.data.repository

import com.example.infracredit.data.local.TokenManager
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: InfracreditApi,
    private val transactionDao: TransactionDao,
    private val tokenManager: TokenManager
) : TransactionRepository {

    override fun getTransactionsFlow(customerId: String): Flow<List<Transaction>> {
        return tokenManager.ownerPhone.flatMapLatest { phone ->
            if (phone != null) {
                transactionDao.getTransactionsForCustomer(customerId, phone)
                    .map { entities -> entities.map { it.toDomain() } }
            } else {
                flowOf(emptyList())
            }
        }
    }

    private suspend fun getOwnerPhone(): String {
        return tokenManager.ownerPhone.firstOrNull() ?: throw Exception("User not authenticated")
    }

    override suspend fun getTransactions(customerId: String): Result<List<Transaction>> {
        return try {
            val ownerPhone = getOwnerPhone()
            val response = api.getTransactions(customerId)
            val transactions = response.map { it.toDomain() }
            
            // Sync to local DB
            transactionDao.insertTransactions(transactions.map { it.toEntity(ownerPhone) })
            
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addTransaction(customerId: String, amount: Double, type: TransactionType, description: String?): Result<Transaction> {
        return try {
            val ownerPhone = getOwnerPhone()
            val response = api.addTransaction(
                TransactionDto(customerId = customerId, amount = amount, type = type.name, description = description)
            )
            val transaction = response.toDomain()
            transactionDao.insertTransaction(transaction.toEntity(ownerPhone))
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(id: String, amount: Double, type: TransactionType, description: String?): Result<Transaction> {
        return try {
            val ownerPhone = getOwnerPhone()
            val response = api.updateTransaction(
                id,
                TransactionDto(id = id, amount = amount, type = type.name, description = description)
            )
            val transaction = response.toDomain()
            transactionDao.insertTransaction(transaction.toEntity(ownerPhone))
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            val ownerPhone = getOwnerPhone()
            api.deleteTransaction(id)
            transactionDao.updateDeleteStatus(id, ownerPhone, 1) // Soft delete
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
