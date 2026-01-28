package com.example.infracredit.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.infracredit.data.local.TokenManager
import com.example.infracredit.data.local.dao.CustomerDao
import com.example.infracredit.data.local.dao.TransactionDao
import com.example.infracredit.data.local.entity.toEntity
import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.toDomain
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: InfracreditApi,
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val tokenManager: TokenManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ownerPhone = tokenManager.ownerPhone.firstOrNull() ?: return Result.failure()

        return try {
            // 1. Fetch Customers
            val customersResponse = api.getCustomers(deleted = false)
            val customerEntities = customersResponse.map { it.toDomain().toEntity(ownerPhone) }
            customerDao.insertCustomers(customerEntities)

            // 2. Fetch Transactions for each customer
            customerEntities.forEach { customer ->
                val transactionsResponse = api.getTransactions(customer.id)
                val transactionEntities = transactionsResponse.map { it.toDomain().toEntity(ownerPhone) }
                transactionDao.insertTransactions(transactionEntities)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
