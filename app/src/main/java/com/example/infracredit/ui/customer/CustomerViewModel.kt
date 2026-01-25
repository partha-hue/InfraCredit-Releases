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
        // Observe real-time flow from repository
        repository.customersFlow
            .onEach { customers ->
                _listState.value = _listState.value.copy(customers = customers, isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun getCustomers(deleted: Boolean = false) {
        viewModelScope.launch {
            if (deleted) _deletedState.value = CustomerListState(isLoading = true)
            else _listState.value = _listState.value.copy(isLoading = true)

            repository.refreshCustomers(deleted)
                .onFailure { e ->
                    if (deleted) _deletedState.value = CustomerListState(error = e.message)
                    else _listState.value = _listState.value.copy(error = e.message, isLoading = false)
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
        viewModelScope.launch {
            repository.updateCustomer(id, name, phone, isDeleted)
        }
    }
}
