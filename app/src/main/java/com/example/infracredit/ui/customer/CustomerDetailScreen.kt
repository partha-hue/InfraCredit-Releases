package com.example.infracredit.ui.customer

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.infracredit.domain.model.Transaction
import com.example.infracredit.domain.model.TransactionType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val state = viewModel.detailState.value
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(TransactionType.CREDIT) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(state.customer?.name ?: "Loading...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (state.customer?.phone != null) {
                            Text(state.customer.phone, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        state.customer?.let {
                            sendWhatsAppReminder(context, it.phone, it.name, it.totalDue)
                        }
                    }) {
                        // Use Share icon as fallback if WhatsApp icon is missing in library
                        Icon(Icons.Default.Share, contentDescription = "Reminder", tint = Color(0xFF25D366))
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { 
                        state.customer?.let {
                            customerViewModel.updateCustomer(it.id, it.name, it.phone, isDeleted = true)
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Balance Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if ((state.customer?.totalDue ?: 0.0) > 0) 
                        Color(0xFFFFEBEE)
                    else Color(0xFFE8F5E9)
                ),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if ((state.customer?.totalDue ?: 0.0) > 0) "Total Credit (Give)" else "Total Payment (Get)", 
                        fontSize = 14.sp,
                        color = if ((state.customer?.totalDue ?: 0.0) > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                    Text(
                        text = "₹ ${Math.abs(state.customer?.totalDue ?: 0.0)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if ((state.customer?.totalDue ?: 0.0) > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        dialogType = TransactionType.PAYMENT
                        showAddDialog = true 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("YOU GOT", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { 
                        dialogType = TransactionType.CREDIT
                        showAddDialog = true 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("YOU GAVE", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transaction History",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray
            )

            // Transactions List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.transactions.reversed()) { tx ->
                    WhatsAppChatBubble(tx)
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            type = dialogType,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, desc ->
                viewModel.addTransaction(amount, dialogType, desc)
                showAddDialog = false
            }
        )
    }

    if (showEditDialog) {
        state.customer?.let { customer ->
            EditCustomerDialog(
                initialName = customer.name,
                initialPhone = customer.phone ?: "",
                onDismiss = { showEditDialog = false },
                onConfirm = { name, phone ->
                    customerViewModel.updateCustomer(customer.id, name, phone)
                    viewModel.loadCustomerData()
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun WhatsAppChatBubble(tx: Transaction) {
    val dateText = remember(tx.createdAt) {
        try {
            val zonedDateTime = ZonedDateTime.parse(tx.createdAt)
            zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            tx.createdAt
        }
    }
    
    val isCredit = tx.type == TransactionType.CREDIT
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCredit) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (isCredit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!tx.description.isNullOrBlank()) {
                    Text(text = tx.description, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "₹ ${tx.amount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                    Text(
                        text = dateText,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

fun sendWhatsAppReminder(context: Context, phone: String?, name: String, amount: Double) {
    if (phone == null) return
    val message = "Hello $name, this is a reminder from InfraCredit. Your current balance is ₹$amount. Please settle it soon. Thank you!"
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

@Composable
fun EditCustomerDialog(
    initialName: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Customer") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, phone) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TransactionType.CREDIT) "You Gave (Credit)" else "You Got (Payment)") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt > 0) onConfirm(amt, description)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
