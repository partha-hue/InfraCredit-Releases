package com.example.infracredit.ui.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.data.remote.dto.ProfileDto
import com.example.infracredit.domain.model.TransactionType
import com.example.infracredit.domain.repository.AuthRepository
import com.example.infracredit.domain.repository.CustomerRepository
import com.example.infracredit.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerIdFromState: String? = savedStateHandle["customerId"]

    private val _detailState = mutableStateOf(CustomerDetailState())
    val detailState: State<CustomerDetailState> = _detailState

    private val _addTxState = mutableStateOf(AddTransactionState())
    val addTxState: State<AddTransactionState> = _addTxState

    private val _ownerProfile = mutableStateOf<ProfileDto?>(null)
    val ownerProfile: State<ProfileDto?> = _ownerProfile

    init {
        loadOwnerProfile()
        
        if (customerIdFromState != null) {
            customerRepository.customersFlow
                .onEach { customers ->
                    val cachedCustomer = customers.find { it.id == customerIdFromState }
                    if (cachedCustomer != null) {
                        _detailState.value = _detailState.value.copy(
                            customer = cachedCustomer
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun loadOwnerProfile() {
        viewModelScope.launch {
            authRepository.getProfile().onSuccess {
                _ownerProfile.value = it
            }
        }
    }

    fun loadCustomerData(id: String? = customerIdFromState) {
        val targetId = id ?: run {
            _detailState.value = _detailState.value.copy(error = "No customer ID provided", isLoading = false)
            return
        }
        
        viewModelScope.launch {
            // Show loading if we have no transactions yet
            if (_detailState.value.transactions.isEmpty()) {
                _detailState.value = _detailState.value.copy(isLoading = true, error = null)
            }

            val customerDeferred = async { customerRepository.getCustomerById(targetId) }
            val transactionsDeferred = async { transactionRepository.getTransactions(targetId) }

            val customerResult = customerDeferred.await()
            val transactionsResult = transactionsDeferred.await()

            if (customerResult.isFailure && transactionsResult.isFailure) {
                _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    error = "Check your internet connection"
                )
            } else {
                _detailState.value = _detailState.value.copy(
                    customer = customerResult.getOrNull() ?: _detailState.value.customer,
                    transactions = transactionsResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error = if (transactionsResult.isFailure) "Failed to load transactions" else null
                )
            }
        }
    }

    fun addTransaction(amount: Double, type: TransactionType, description: String?, customerId: String? = customerIdFromState) {
        val targetId = customerId ?: return
        viewModelScope.launch {
            _addTxState.value = _addTxState.value.copy(isLoading = true)
            val result = transactionRepository.addTransaction(targetId, amount, type, description)
            if (result.isSuccess) {
                _addTxState.value = AddTransactionState(isSuccess = true)
                loadCustomerData(targetId)
                customerRepository.refreshCustomers()
            } else {
                _addTxState.value = _addTxState.value.copy(
                    isLoading = false, 
                    error = result.exceptionOrNull()?.message ?: "Transaction failed"
                )
            }
        }
    }

    fun deleteCustomer(onSuccess: () -> Unit) {
        val id = customerIdFromState ?: return
        viewModelScope.launch {
            customerRepository.deleteCustomer(id).onSuccess {
                customerRepository.refreshCustomers()
                onSuccess()
            }
        }
    }
    
    fun resetAddTxState() {
        _addTxState.value = AddTransactionState()
    }
}
