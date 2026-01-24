package com.example.infracredit.data.repository

import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.CustomerDto
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.domain.repository.CustomerRepository
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val api: InfracreditApi
) : CustomerRepository {

    override suspend fun getCustomers(): Result<List<Customer>> {
        return try {
            val dtos = api.getCustomers()
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCustomer(name: String, phone: String?): Result<Customer> {
        return try {
            val dto = api.addCustomer(CustomerDto(name = name, phone = phone))
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomerById(id: String): Result<Customer> {
        // In a real app, this might be a specific API call or filtered from list
        return try {
            val dtos = api.getCustomers()
            val customer = dtos.find { it.id == id }?.toDomain()
            if (customer != null) Result.success(customer) 
            else Result.failure(Exception("Customer not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CustomerDto.toDomain() = Customer(
        id = id ?: "",
        name = name,
        phone = phone,
        totalDue = totalDue,
        createdAt = createdAt ?: 0L
    )
}