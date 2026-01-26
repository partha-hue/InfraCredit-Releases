package com.example.infracredit.ui.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _listState = mutableStateOf(CustomerListState())
    val listState: State<CustomerListState> = _listState

    private val _deletedState = mutableStateOf(CustomerListState())
    val deletedState: State<CustomerListState> = _deletedState

    private val _addState = mutableStateOf(AddCustomerState())
    val addState: State<AddCustomerState> = _addState

    init {
        // Observe real-time flow from repository for active customers
        repository.customersFlow
            .onEach { customers ->
                _listState.value = _listState.value.copy(
                    customers = customers, 
                    isLoading = false,
                    error = null
                )
            }
            .launchIn(viewModelScope)
    }

    fun getCustomers(deleted: Boolean = false) {
        viewModelScope.launch {
            // Show loading if we have zero data
            if (deleted) {
                if (_deletedState.value.customers.isEmpty()) {
                    _deletedState.value = _deletedState.value.copy(isLoading = true, error = null)
                }
            } else {
                if (_listState.value.customers.isEmpty()) {
                    _listState.value = _listState.value.copy(isLoading = true, error = null)
                }
            }

            repository.refreshCustomers(deleted)
                .onSuccess { customers ->
                    if (deleted) {
                        _deletedState.value = _deletedState.value.copy(
                            customers = customers,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _listState.value = _listState.value.copy(
                            customers = customers,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    if (deleted) {
                        _deletedState.value = _deletedState.value.copy(error = e.message, isLoading = false)
                    } else {
                        _listState.value = _listState.value.copy(error = e.message, isLoading = false)
                    }
                }
        }
    }

    fun addCustomer(name: String, phone: String?) {
        viewModelScope.launch {
            _addState.value = AddCustomerState(isLoading = true)
            repository.addCustomer(name, phone)
                .onSuccess {
                    _addState.value = AddCustomerState(isSuccess = true)
                }
                .onFailure { e ->
                    _addState.value = AddCustomerState(error = e.message)
                }
        }
    }

    fun updateCustomer(id: String, name: String, phone: String?, isDeleted: Boolean = false) {
        // OPTIMISTIC UI UPDATE: Remove from current list immediately for better performance
        if (isDeleted) {
            _listState.value = _listState.value.copy(
                customers = _listState.value.customers.filter { it.id != id }
            )
        } else {
            _deletedState.value = _deletedState.value.copy(
                customers = _deletedState.value.customers.filter { it.id != id }
            )
        }

        viewModelScope.launch {
            repository.updateCustomer(id, name, phone, isDeleted)
        }
    }
}
