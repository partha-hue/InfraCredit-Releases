package com.example.infracredit.data.repository

import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.CustomerDto
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.domain.repository.CustomerRepository
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val api: InfracreditApi
) : CustomerRepository {

    override suspend fun getCustomers(deleted: Boolean): Result<List<Customer>> {
        return try {
            val dtos = api.getCustomers(deleted)
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
        return try {
            val dto = api.getCustomer(id)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCustomer(id: String, name: String, phone: String?, isDeleted: Boolean): Result<Customer> {
        return try {
            val dto = api.updateCustomer(id, CustomerDto(name = name, phone = phone, isDeleted = isDeleted))
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(id: String): Result<Boolean> {
        return try {
            val response = api.deleteCustomer(id)
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CustomerDto.toDomain() = Customer(
        id = id ?: "",
        name = name,
        phone = phone,
        totalDue = totalDue,
        createdAt = createdAt ?: ""
    )
}
