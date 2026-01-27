package com.example.infracredit.data.repository

import com.example.infracredit.data.local.dao.CustomerDao
import com.example.infracredit.data.local.entity.toDomain
import com.example.infracredit.data.local.entity.toEntity
import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.CustomerDto
import com.example.infracredit.data.remote.dto.toDomain
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val api: InfracreditApi,
    private val customerDao: CustomerDao
) : CustomerRepository {

    override val customersFlow: Flow<List<Customer>> = customerDao.getAllCustomers()
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshCustomers(deleted: Boolean): Result<List<Customer>> {
        return try {
            val response = api.getCustomers(deleted)
            val customers = response.map { it.toDomain() }
            
            if (!deleted) {
                // Only sync active customers to local DB for primary view
                customerDao.insertCustomers(customers.map { it.toEntity() })
            }
            
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCustomer(name: String, phone: String?): Result<Customer> {
        return try {
            val response = api.addCustomer(CustomerDto(name = name, phone = phone))
            val customer = response.toDomain()
            customerDao.insertCustomer(customer.toEntity())
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomerById(id: String): Result<Customer> {
        return try {
            val response = api.getCustomer(id)
            val customer = response.toDomain()
            customerDao.insertCustomer(customer.toEntity())
            Result.success(customer)
        } catch (e: Exception) {
            val local = customerDao.getCustomerById(id)
            if (local != null) Result.success(local.toDomain())
            else Result.failure(e)
        }
    }

    override suspend fun updateCustomer(id: String, name: String, phone: String?, isDeleted: Boolean): Result<Customer> {
        return try {
            val response = api.updateCustomer(id, CustomerDto(id = id, name = name, phone = phone, isDeleted = isDeleted))
            val customer = response.toDomain()
            customerDao.insertCustomer(customer.toEntity())
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(id: String): Result<Boolean> {
        return try {
            val response = api.deleteCustomer(id)
            customerDao.deleteCustomer(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
