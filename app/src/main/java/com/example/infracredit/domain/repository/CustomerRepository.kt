package com.example.infracredit.domain.repository

import com.example.infracredit.domain.model.Customer

interface CustomerRepository {
    suspend fun getCustomers(): Result<List<Customer>>
    suspend fun addCustomer(name: String, phone: String?): Result<Customer>
    suspend fun getCustomerById(id: String): Result<Customer>
}