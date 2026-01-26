package com.example.infracredit.data.repository

import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.TransactionDto
import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType
import com.example.infracredit.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: InfracreditApi
) : TransactionRepository {

    override suspend fun getTransactions(customerId: String): Result<List<Transaction>> {
        return try {
            val dtos = api.getTransactions(customerId)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addTransaction(
        customerId: String,
        amount: Double,
        type: TransactionType,
        description: String?
    ): Result<Transaction> {
        return try {
            val dto = api.addTransaction(
                TransactionDto(
                    customerId = customerId,
                    amount = amount,
                    type = type.name,
                    description = description
                )
            )
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun TransactionDto.toDomain(): Transaction {
        return Transaction(
            id = id ?: "",
            customerId = customerId ?: "",
            amount = amount ?: 0.0,
            type = try {
                TransactionType.valueOf((type ?: "CREDIT").uppercase())
            } catch (e: Exception) {
                if (type?.contains("pay", ignoreCase = true) == true) TransactionType.PAYMENT 
                else TransactionType.CREDIT
            },
            description = description,
            createdAt = createdAt ?: ""
        )
    }
}
