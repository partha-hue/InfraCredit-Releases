package com.example.infracredit.ui.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun getCustomers(deleted: Boolean = false) {
        viewModelScope.launch {
            if (deleted) _deletedState.value = CustomerListState(isLoading = true)
            else _listState.value = CustomerListState(isLoading = true)

            repository.getCustomers(deleted)
                .onSuccess { customers ->
                    if (deleted) _deletedState.value = CustomerListState(customers = customers)
                    else _listState.value = CustomerListState(customers = customers)
                }
                .onFailure { e ->
                    if (deleted) _deletedState.value = CustomerListState(error = e.message)
                    else _listState.value = CustomerListState(error = e.message)
                }
        }
    }

    fun addCustomer(name: String, phone: String?) {
        viewModelScope.launch {
            _addState.value = AddCustomerState(isLoading = true)
            repository.addCustomer(name, phone)
                .onSuccess {
                    _addState.value = AddCustomerState(isSuccess = true)
                    getCustomers(false)
                }
                .onFailure { e ->
                    _addState.value = AddCustomerState(error = e.message)
                }
        }
    }

    fun updateCustomer(id: String, name: String, phone: String?, isDeleted: Boolean = false) {
        viewModelScope.launch {
            repository.updateCustomer(id, name, phone, isDeleted)
                .onSuccess {
                    getCustomers(false)
                    getCustomers(true)
                }
        }
    }
}
