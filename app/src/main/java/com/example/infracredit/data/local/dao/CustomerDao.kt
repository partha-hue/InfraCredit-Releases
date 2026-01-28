package com.example.infracredit.data.local.dao

import androidx.room.*
import com.example.infracredit.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE ownerPhone = :ownerPhone AND isDeleted = 0 ORDER BY name ASC")
    fun getAllCustomers(ownerPhone: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE ownerPhone = :ownerPhone AND isDeleted = 1 ORDER BY name ASC")
    fun getDeletedCustomers(ownerPhone: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id AND ownerPhone = :ownerPhone")
    suspend fun getCustomerById(id: String, ownerPhone: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET isDeleted = :isDeleted WHERE id = :id AND ownerPhone = :ownerPhone")
    suspend fun updateDeleteStatus(id: String, ownerPhone: String, isDeleted: Int)

    @Query("DELETE FROM customers WHERE id = :id AND ownerPhone = :ownerPhone")
    suspend fun deleteCustomerPermanently(id: String, ownerPhone: String)

    @Query("SELECT * FROM customers WHERE isSynced = 0 AND ownerPhone = :ownerPhone")
    suspend fun getUnsyncedCustomers(ownerPhone: String): List<CustomerEntity>

    @Query("DELETE FROM customers WHERE ownerPhone = :ownerPhone")
    suspend fun clearAllForUser(ownerPhone: String)

    @Query("DELETE FROM customers")
    suspend fun clearAll()
}
