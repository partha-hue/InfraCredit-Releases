package com.example.infracredit.domain.repository

import com.example.infracredit.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    // Real-time flow of customers
    val customersFlow: Flow<List<Customer>>
    
    // Manual triggers
    suspend fun refreshCustomers(deleted: Boolean = false): Result<Unit>
    
    suspend fun addCustomer(name: String, phone: String?): Result<Customer>
    suspend fun getCustomerById(id: String): Result<Customer>
    suspend fun updateCustomer(id: String, name: String, phone: String?, isDeleted: Boolean = false): Result<Customer>
    suspend fun deleteCustomer(id: String): Result<Boolean>
}
