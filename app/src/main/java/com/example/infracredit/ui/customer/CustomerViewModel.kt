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

    private val _addState = mutableStateOf(AddCustomerState())
    val addState: State<AddCustomerState> = _addState

    fun getCustomers() {
        viewModelScope.launch {
            _listState.value = CustomerListState(isLoading = true)
            val result = repository.getCustomers()
            if (result.isSuccess) {
                _listState.value = CustomerListState(customers = result.getOrDefault(emptyList()))
            } else {
                _listState.value = CustomerListState(error = result.exceptionOrNull()?.message ?: "Failed to load customers")
            }
        }
    }

    fun addCustomer(name: String, phone: String?) {
        viewModelScope.launch {
            _addState.value = AddCustomerState(isLoading = true)
            val result = repository.addCustomer(name, phone)
            if (result.isSuccess) {
                _addState.value = AddCustomerState(isSuccess = true)
                getCustomers() // Refresh list
            } else {
                _addState.value = AddCustomerState(error = result.exceptionOrNull()?.message ?: "Failed to add customer")
            }
        }
    }
}