package com.example.infracredit.ui.customer

import com.example.infracredit.domain.model.Customer
import com.example.infracredit.domain.model.Transaction

data class CustomerDetailState(
    val isLoading: Boolean = false,
    val customer: Customer? = null,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
)

data class AddTransactionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)