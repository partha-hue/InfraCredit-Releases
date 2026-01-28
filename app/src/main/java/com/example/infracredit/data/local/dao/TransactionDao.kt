package com.example.infracredit.data.local.dao

import androidx.room.*
import com.example.infracredit.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND ownerPhone = :ownerPhone AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getTransactionsForCustomer(customerId: String, ownerPhone: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE ownerPhone = :ownerPhone AND isDeleted = 1 ORDER BY createdAt DESC")
    fun getDeletedTransactions(ownerPhone: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = :isDeleted WHERE id = :id AND ownerPhone = :ownerPhone")
    suspend fun updateDeleteStatus(id: String, ownerPhone: String, isDeleted: Int)

    @Query("DELETE FROM transactions WHERE id = :id AND ownerPhone = :ownerPhone")
    suspend fun deleteTransactionPermanently(id: String, ownerPhone: String)

    @Query("SELECT * FROM transactions WHERE isSynced = 0 AND ownerPhone = :ownerPhone")
    suspend fun getUnsyncedTransactions(ownerPhone: String): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE ownerPhone = :ownerPhone")
    suspend fun clearAllForUser(ownerPhone: String)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
