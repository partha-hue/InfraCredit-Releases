package com.example.infracredit.ui.customer

import com.example.infracredit.domain.model.Customer

data class CustomerListState(
    val isLoading: Boolean = false,
    val customers: List<Customer> = emptyList(),
    val error: String? = null
)

data class AddCustomerState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)