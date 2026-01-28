package com.example.infracredit.data.repository

import com.example.infracredit.data.local.TokenManager
import com.example.infracredit.data.local.dao.CustomerDao
import com.example.infracredit.data.local.entity.toDomain
import com.example.infracredit.data.local.entity.toEntity
import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.CustomerDto
import com.example.infracredit.data.remote.dto.toDomain
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val api: InfracreditApi,
    private val customerDao: CustomerDao,
    private val tokenManager: TokenManager
) : CustomerRepository {

    override val customersFlow: Flow<List<Customer>> = tokenManager.ownerPhone.flatMapLatest { phone ->
        if (phone != null) {
            customerDao.getAllCustomers(phone).map { entities -> entities.map { it.toDomain() } }
        } else {
            flowOf(emptyList())
        }
    }

    private suspend fun getOwnerPhone(): String {
        return tokenManager.ownerPhone.firstOrNull() ?: throw Exception("User not authenticated")
    }

    override suspend fun refreshCustomers(deleted: Boolean): Result<List<Customer>> {
        return try {
            val ownerPhone = getOwnerPhone()
            val response = api.getCustomers(ownerPhone, deleted)
            val customers = response.map { it.toDomain() }
            
            if (!deleted) {
                customerDao.insertCustomers(customers.map { it.toEntity(ownerPhone) })
            }
            
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCustomer(name: String, phone: String?): Result<Customer> {
        return try {
            val ownerPhone = getOwnerPhone()
            val response = api.addCustomer(CustomerDto(name = name, phone = phone))
            val customer = response.toDomain()
            customerDao.insertCustomer(customer.toEntity(ownerPhone))
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomerById(id: String): Result<Customer> {
        return try {
            val ownerPhone = getOwnerPhone()
            val response = api.getCustomer(id)
            val customer = response.toDomain()
            customerDao.insertCustomer(customer.toEntity(ownerPhone))
            Result.success(customer)
        } catch (e: Exception) {
            val ownerPhone = try { getOwnerPhone() } catch (ex: Exception) { null }
            val local = if (ownerPhone != null) customerDao.getCustomerById(id, ownerPhone) else null
            if (local != null) Result.success(local.toDomain())
            else Result.failure(e)
        }
    }

    override suspend fun updateCustomer(id: String, name: String, phone: String?, isDeleted: Boolean): Result<Customer> {
        return try {
            val ownerPhone = getOwnerPhone()
            val response = api.updateCustomer(id, CustomerDto(id = id, name = name, phone = phone, isDeleted = isDeleted))
            val customer = response.toDomain()
            customerDao.insertCustomer(customer.toEntity(ownerPhone, isDeleted = if (isDeleted) 1 else 0))
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(id: String): Result<Boolean> {
        return try {
            val ownerPhone = getOwnerPhone()
            api.deleteCustomer(id)
            customerDao.updateDeleteStatus(id, ownerPhone, 1) // Soft delete
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
