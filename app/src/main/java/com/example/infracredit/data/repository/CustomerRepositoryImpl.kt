package com.example.infracredit.data.repository

import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.CustomerDto
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val api: InfracreditApi
) : CustomerRepository {

    private val _customersFlow = MutableStateFlow<List<Customer>>(emptyList())
    override val customersFlow: Flow<List<Customer>> = _customersFlow.asStateFlow()

    override suspend fun refreshCustomers(deleted: Boolean): Result<Unit> {
        return try {
            val dtos = api.getCustomers(deleted)
            val domainList = dtos.map { it.toDomain() }
            _customersFlow.emit(domainList)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCustomer(name: String, phone: String?): Result<Customer> {
        return try {
            val dto = api.addCustomer(CustomerDto(name = name, phone = phone))
            val customer = dto.toDomain()
            // Optimistic update or simple refresh
            refreshCustomers()
            Result.success(customer)
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
            val customer = dto.toDomain()
            refreshCustomers()
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(id: String): Result<Boolean> {
        return try {
            val response = api.deleteCustomer(id)
            refreshCustomers()
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
