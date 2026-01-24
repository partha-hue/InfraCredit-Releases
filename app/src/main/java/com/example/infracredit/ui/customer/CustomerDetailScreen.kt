package com.example.infracredit.ui.customer

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.infracredit.ui.theme.InfraCreditTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

    LaunchedEffect(Unit) {
        viewModel.loadCustomerData()
    }

    // Force Light Theme for this screen
    InfraCreditTheme(forceLight = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(state.customer?.name ?: "Loading...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("View Profile", fontSize = 12.sp, color = Color(0xFF00A884), fontWeight = FontWeight.Medium)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Call logic */ }) {
                            Icon(Icons.Default.Call, contentDescription = "Call")
                        }
                        IconButton(onClick = { /* Info logic */ }) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Info")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column {
                        // Balance Info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Balance Due", fontSize = 14.sp, color = Color.Gray)
                            Text(
                                text = "₹ ${String.format("%.0f", Math.abs(state.customer?.totalDue ?: 0.0))}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if ((state.customer?.totalDue ?: 0.0) > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            )
                        }
                        
                        // Action Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { 
                                    dialogType = TransactionType.PAYMENT
                                    showAddDialog = true 
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Received", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { 
                                    dialogType = TransactionType.CREDIT
                                    showAddDialog = true 
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Given", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFE5DDD5)) // WhatsApp background color
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val grouped = state.transactions.groupBy { 
                        try {
                            ZonedDateTime.parse(it.createdAt).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        } catch (e: Exception) {
                            "Recent"
                        }
                    }

                    grouped.forEach { (date, txs) ->
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Surface(
                                    color = Color(0xFFD1E4EF),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = date,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF4A5F6B)
                                    )
                                }
                            }
                        }
                        items(txs) { tx ->
                            WhatsAppTransactionBubble(tx, state.customer?.name ?: "")
                        }
                    }
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
}

@Composable
fun WhatsAppTransactionBubble(tx: Transaction, customerName: String) {
    val isCredit = tx.type == TransactionType.CREDIT
    val timeText = try {
        ZonedDateTime.parse(tx.createdAt).format(DateTimeFormatter.ofPattern("hh:mm a"))
    } catch (e: Exception) {
        ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCredit) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isCredit) Alignment.End else Alignment.Start) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomStart = if (isCredit) 8.dp else 0.dp,
                    bottomEnd = if (isCredit) 0.dp else 8.dp
                ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column {
                    // Header "Added by..."
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE1ECEF))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Added by $customerName...",
                            fontSize = 11.sp,
                            color = Color(0xFF6B8A8D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Content Row
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "₹${String.format("%.0f", tx.amount)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = timeText,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            // Due Amount text below bubble
            Text(
                text = "₹${String.format("%.0f", tx.amount)} Due",
                fontSize = 11.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
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
