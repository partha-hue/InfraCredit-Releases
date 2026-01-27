package com.example.infracredit.data.local.dao

import androidx.room.*
import com.example.infracredit.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: String)

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsyncedCustomers(): List<CustomerEntity>

    @Query("DELETE FROM customers")
    suspend fun clearAll()
}
