package com.example.infracredit.domain.repository

import com.example.infracredit.domain.model.Customer

interface CustomerRepository {
    suspend fun getCustomers(deleted: Boolean = false): Result<List<Customer>>
    suspend fun addCustomer(name: String, phone: String?): Result<Customer>
    suspend fun getCustomerById(id: String): Result<Customer>
    suspend fun updateCustomer(id: String, name: String, phone: String?, isDeleted: Boolean = false): Result<Customer>
    suspend fun deleteCustomer(id: String): Result<Boolean>
}
