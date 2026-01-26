package com.example.infracredit.ui.customer

import android.telephony.SmsManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.data.remote.dto.ProfileDto
import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType
import com.example.infracredit.domain.repository.AuthRepository
import com.example.infracredit.domain.repository.CustomerRepository
import com.example.infracredit.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs

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
                    transactions = transactionsResult.getOrDefault(emptyList()).sortedBy { it.createdAt },
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
                val transaction = result.getOrNull()
                val customer = _detailState.value.customer
                val owner = _ownerProfile.value
                
                if (customer?.phone != null && transaction != null) {
                    val newTotalDue = customer.totalDue + (if(type == TransactionType.CREDIT) amount else -amount)
                    sendAutomaticSms(
                        customerName = customer.name, 
                        phoneNumber = customer.phone, 
                        amount = amount, 
                        type = type, 
                        totalBalance = newTotalDue,
                        ownerName = owner?.fullName ?: "Shop Owner",
                        isEdit = false
                    )
                }

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

    fun updateTransaction(transactionId: String, amount: Double, type: TransactionType, description: String?) {
        viewModelScope.launch {
            _addTxState.value = _addTxState.value.copy(isLoading = true)
            val result = transactionRepository.updateTransaction(transactionId, amount, type, description)
            if (result.isSuccess) {
                val transaction = result.getOrNull()
                val customer = _detailState.value.customer
                val owner = _ownerProfile.value

                // We need the updated total due for the SMS
                // Refreshing customers will get the new total due from backend
                customerRepository.refreshCustomers()
                val updatedCustomer = customerRepository.getCustomerById(customerIdFromState ?: "").getOrNull()
                
                if (updatedCustomer?.phone != null && transaction != null) {
                    sendAutomaticSms(
                        customerName = updatedCustomer.name,
                        phoneNumber = updatedCustomer.phone,
                        amount = amount,
                        type = type,
                        totalBalance = updatedCustomer.totalDue,
                        ownerName = owner?.fullName ?: "Shop Owner",
                        isEdit = true
                    )
                }

                _addTxState.value = AddTransactionState(isSuccess = true)
                loadCustomerData()
            } else {
                _addTxState.value = _addTxState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Update failed"
                )
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _addTxState.value = _addTxState.value.copy(isLoading = true)
            val result = transactionRepository.deleteTransaction(transactionId)
            if (result.isSuccess) {
                customerRepository.refreshCustomers()
                _addTxState.value = AddTransactionState(isSuccess = true)
                loadCustomerData()
            } else {
                _addTxState.value = _addTxState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Delete failed"
                )
            }
        }
    }

    private fun sendAutomaticSms(customerName: String, phoneNumber: String, amount: Double, type: TransactionType, totalBalance: Double, ownerName: String, isEdit: Boolean) {
        try {
            val prefix = if (isEdit) "UPDATE: " else ""
            val typeString = if (type == TransactionType.CREDIT) "Given (Credit)" else "Received (Payment)"
            val balanceLabel = if (totalBalance >= 0) "Total Due" else "Advance"
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"))
            
            val message = "${prefix}Dear $customerName, ₹$amount ($typeString) recorded at $currentTime. $balanceLabel: ₹${abs(totalBalance)}. - Sent by $ownerName via InfraCredit"
            
            val smsManager: SmsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
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
