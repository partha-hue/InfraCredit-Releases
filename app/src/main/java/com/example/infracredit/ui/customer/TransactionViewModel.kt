package com.example.infracredit.ui.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.domain.model.TransactionType
import com.example.infracredit.domain.repository.CustomerRepository
import com.example.infracredit.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerIdFromState: String? = savedStateHandle["customerId"]

    private val _detailState = mutableStateOf(CustomerDetailState())
    val detailState: State<CustomerDetailState> = _detailState

    private val _addTxState = mutableStateOf(AddTransactionState())
    val addTxState: State<AddTransactionState> = _addTxState

    init {
        customerIdFromState?.let { loadCustomerData(it) }
    }

    fun loadCustomerData(id: String? = customerIdFromState) {
        val targetId = id ?: return
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true)
            val customerResult = customerRepository.getCustomerById(targetId)
            val transactionsResult = transactionRepository.getTransactions(targetId)

            if (customerResult.isSuccess && transactionsResult.isSuccess) {
                _detailState.value = CustomerDetailState(
                    customer = customerResult.getOrNull(),
                    transactions = transactionsResult.getOrDefault(emptyList())
                )
            } else {
                _detailState.value = CustomerDetailState(
                    error = "Failed to load customer details"
                )
            }
        }
    }

    fun addTransaction(amount: Double, type: TransactionType, description: String?, customerId: String? = customerIdFromState) {
        val targetId = customerId ?: return
        viewModelScope.launch {
            _addTxState.value = AddTransactionState(isLoading = true)
            val result = transactionRepository.addTransaction(targetId, amount, type, description)
            if (result.isSuccess) {
                _addTxState.value = AddTransactionState(isSuccess = true)
                if (targetId == customerIdFromState) {
                    loadCustomerData(targetId) // Refresh if it's the current detail screen
                }
            } else {
                _addTxState.value = AddTransactionState(error = result.exceptionOrNull()?.message ?: "Transaction failed")
            }
        }
    }
    
    fun resetAddTxState() {
        _addTxState.value = AddTransactionState()
    }
}
