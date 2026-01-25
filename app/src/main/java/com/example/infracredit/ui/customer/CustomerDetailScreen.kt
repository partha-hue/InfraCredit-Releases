package com.example.infracredit.ui.customer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state = viewModel.detailState.value
    val addTxState = viewModel.addTxState.value
    val ownerProfile = viewModel.ownerProfile.value
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(TransactionType.CREDIT) }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadCustomerData()
    }

    // Scroll to bottom when transactions are loaded or updated
    LaunchedEffect(state.transactions.size) {
        if (state.transactions.isNotEmpty()) {
            val totalItems = state.transactions.size + state.transactions.groupBy { it.createdAt.split("T")[0] }.size
            listState.animateScrollToItem(maxOf(0, totalItems - 1))
        }
    }

    LaunchedEffect(addTxState.isSuccess) {
        if (addTxState.isSuccess) {
            viewModel.resetAddTxState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable { 
                        state.customer?.id?.let { onNavigateToEdit(it) }
                    }) {
                        Text(
                            text = state.customer?.name ?: "Loading...", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View Profile", 
                            fontSize = 12.sp, 
                            color = Color(0xFF00A884), 
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        state.customer?.phone?.let { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        } ?: run {
                            Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { viewModel.loadCustomerData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Customer") },
                                onClick = { 
                                    showMenu = false
                                    state.customer?.id?.let { onNavigateToEdit(it) }
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Customer") },
                                onClick = { 
                                    showMenu = false
                                    viewModel.deleteCustomer { onNavigateBack() }
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.clickable { /* Report Logic */ },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Report", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(16.dp))
                        }
                        
                        Button(
                            onClick = { 
                                state.customer?.phone?.let { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.width(140.dp).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3B)),
                            shape = RoundedCornerShape(21.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Call", fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Balance Due", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val balance = state.customer?.totalDue ?: 0.0
                            Text(
                                text = "₹ ${String.format("%,.0f", abs(balance))}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (balance >= 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (balance >= 0) Color.Red else Color(0xFF388E3C), modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                dialogType = TransactionType.PAYMENT
                                showAddDialog = true 
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Received", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Button(
                            onClick = { 
                                dialogType = TransactionType.CREDIT
                                showAddDialog = true 
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF006D3B).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF006D3B), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Given", color = Color(0xFF006D3B), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        val isDark = isSystemInDarkTheme()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(if (isDark) Color(0xFF0B141B) else Color(0xFFECE5DD))
        ) {
            if ((state.customer?.totalDue ?: 0.0) > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = if (isDark) Color(0xFF1F2C34) else Color(0xFFE7F3EF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable {
                                state.customer?.let { customer ->
                                    val message = "Hello ${customer.name}, your total due is ₹${customer.totalDue}. Please clear it soon. Thank you!"
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse("https://api.whatsapp.com/send?phone=${customer.phone}&text=${Uri.encode(message)}")
                                    context.startActivity(intent)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF00A884))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Send WhatsApp Reminder", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                            Text("Remind ${state.customer?.name} about ₹${abs(state.customer?.totalDue ?: 0.0)}", fontSize = 12.sp, color = if (isDark) Color.LightGray else Color.DarkGray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            if (state.isLoading && state.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00A884))
                }
            } else if (state.error != null && state.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Red)
                        Text(state.error, color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.loadCustomerData() }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (state.transactions.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Text("No transactions yet", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCustomerData() }) {
                            Text("Refresh")
                        }
                    }
                }
            } else {
                // Pre-calculate running balances for labels
                val runningBalances = remember(state.transactions) {
                    val balances = mutableMapOf<String, Double>()
                    var current = 0.0
                    state.transactions.sortedBy { it.createdAt }.forEach { tx ->
                        if (tx.type == TransactionType.CREDIT) {
                            current += tx.amount
                        } else {
                            current -= tx.amount
                        }
                        balances[tx.id] = current
                    }
                    balances
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val grouped = state.transactions.groupBy { 
                        try {
                            OffsetDateTime.parse(it.createdAt)
                                .atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        } catch (e: Exception) {
                            try {
                                it.createdAt.split("T")[0] 
                            } catch (e2: Exception) {
                                "Recent"
                            }
                        }
                    }

                    grouped.forEach { (date, txs) ->
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                Surface(
                                    color = if (isDark) Color(0xFF182229) else Color(0xFF80A1A2),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = date,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        items(txs.sortedBy { it.createdAt }, key = { it.id }) { tx ->
                            WhatsAppTransactionBubble(
                                tx = tx, 
                                ownerName = ownerProfile?.fullName ?: "Owner",
                                balance = runningBalances[tx.id]
                            )
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
fun WhatsAppTransactionBubble(tx: Transaction, ownerName: String, balance: Double?) {
    val isGiven = tx.type == TransactionType.CREDIT 
    val isDark = isSystemInDarkTheme()
    val timeText = try {
        if (tx.createdAt.isEmpty()) "" else {
            OffsetDateTime.parse(tx.createdAt)
                .atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("hh:mm a"))
        }
    } catch (e: Exception) {
        try {
            val instant = Instant.parse(tx.createdAt)
            OffsetDateTime.ofInstant(instant, ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e2: Exception) {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isGiven) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = if (isGiven) 8.dp else 0.dp,
                bottomEnd = if (isGiven) 0.dp else 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1F2C34) else Color.White
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF232D36) else Color(0xFFD1E3E1))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Added by ${ownerName.take(15)}...",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF8696A0) else Color(0xFF667781),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isGiven) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isDark) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${String.format("%,.0f", tx.amount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = timeText,
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF8696A0) else Color(0xFF667781)
                    )
                }

                if (!tx.description.isNullOrBlank()) {
                    Text(
                        text = tx.description,
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFFD1D7DB) else Color(0xFF54656F),
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
                    )
                }
            }
        }
        
        if (balance != null) {
            Text(
                text = "₹${String.format("%,.0f", abs(balance))} Due",
                fontSize = 12.sp,
                color = if (isDark) Color(0xFF8696A0) else Color(0xFF667781),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
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
        title = { Text(if (type == TransactionType.CREDIT) "You Gave (Credit)" else "You Got (Received)") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, description)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
