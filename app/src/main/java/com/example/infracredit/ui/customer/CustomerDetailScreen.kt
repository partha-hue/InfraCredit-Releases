package com.example.infracredit.ui.customer

import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
    val scope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(TransactionType.CREDIT) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Pre-calculate running balances for consistency
    val transactionsWithBalance = remember(state.transactions) {
        var balance = 0.0
        state.transactions.map { tx ->
            balance += if (tx.type == TransactionType.CREDIT) tx.amount else -tx.amount
            tx to balance
        }
    }

    // Use calculated balance for UI consistency
    val totalDue = transactionsWithBalance.lastOrNull()?.second ?: 0.0

    val showScrollToBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem < listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCustomerData()
    }

    LaunchedEffect(state.transactions.size) {
        if (state.transactions.isNotEmpty()) {
            listState.animateScrollToItem(state.transactions.size + state.transactions.groupBy { it.createdAt.split("T")[0] }.size)
        }
    }

    LaunchedEffect(addTxState.isSuccess) {
        if (addTxState.isSuccess) {
            val customer = state.customer
            val lastTx = addTxState.lastTransaction
            if (customer != null && lastTx != null && !customer.phone.isNullOrBlank()) {
                try {
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    val type = if (lastTx.type == TransactionType.CREDIT) "Credit" else "Payment"
                    val label = if (totalDue >= 0) "Total Due" else "Advance"
                    val message = "Dear ${customer.name}, ₹${lastTx.amount} ($type) recorded. $label: ₹${abs(totalDue)}. - Sent via InfraCredit"
                    smsManager.sendTextMessage(customer.phone, null, message, null, null)
                    Toast.makeText(context, "Automatic SMS Sent to ${customer.name}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.resetAddTxState()
            showAddDialog = false
            editingTransaction = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable { 
                        state.customer?.id?.let { onNavigateToEdit(it) }
                    }) {
                        Text(text = state.customer?.name ?: "Loading...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "View Profile", fontSize = 12.sp, color = Color(0xFF00A884), fontWeight = FontWeight.Medium)
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
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
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
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.clickable { /* Report Logic */ }, verticalAlignment = Alignment.CenterVertically) {
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Balance Due", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹ ${String.format(Locale.getDefault(), "%,.0f", abs(totalDue))}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (totalDue >= 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (totalDue >= 0) Color.Red else Color(0xFF388E3C), modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                dialogType = TransactionType.PAYMENT
                                editingTransaction = null
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
                                editingTransaction = null
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
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToBottom,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(state.transactions.size + 10)
                        }
                    },
                    modifier = Modifier.size(40.dp).padding(bottom = 0.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFF00A884)
                ) {
                    Icon(Icons.Default.KeyboardDoubleArrowDown, contentDescription = "Scroll to bottom")
                }
            }
        }
    ) { padding ->
        val isDark = isSystemInDarkTheme()
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(if (isDark) Color(0xFF0B141B) else Color(0xFFECE5DD))) {
            if (state.isLoading && state.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00A884))
                }
            } else {
                
                state.customer?.let { customer ->
                    if (totalDue != 0.0) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1F2C34) else Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF00A884), modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Send WhatsApp Reminder", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Remind ${customer.name} about ₹${abs(totalDue)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val grouped = transactionsWithBalance.groupBy { it.first.createdAt.split("T")[0] }

                    grouped.forEach { (date, txAndBalanceList) ->
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                Surface(color = if (isDark) Color(0xFF182229) else Color(0xFF80A1A2), shape = RoundedCornerShape(12.dp)) {
                                    Text(text = getDisplayDate(date), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                        items(txAndBalanceList, key = { it.first.id }) { (tx, balance) ->
                            WhatsAppTransactionBubble(
                                tx = tx, 
                                ownerName = ownerProfile?.fullName ?: "Owner",
                                runningBalance = balance,
                                onLongClick = {
                                    editingTransaction = tx
                                    dialogType = tx.type
                                    showAddDialog = true
                                }
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
            existingTransaction = editingTransaction,
            onDismiss = { 
                showAddDialog = false
                editingTransaction = null
            },
            onConfirm = { amount, desc ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(editingTransaction!!.id, amount, editingTransaction!!.type, desc)
                } else {
                    viewModel.addTransaction(amount, dialogType, desc)
                }
            },
            onDelete = {
                editingTransaction?.id?.let { viewModel.deleteTransaction(it) }
            }
        )
    }
}

fun getDisplayDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateStr) ?: return dateStr
        
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = fmt.format(today)
        val yesterdayStr = fmt.format(yesterday)
        
        when (dateStr) {
            todayStr -> "Today"
            yesterdayStr -> "Yesterday"
            else -> {
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        dateStr
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WhatsAppTransactionBubble(
    tx: Transaction, 
    ownerName: String, 
    runningBalance: Double,
    onLongClick: () -> Unit
) {
    val isGiven = tx.type == TransactionType.CREDIT 
    val isDark = isSystemInDarkTheme()
    
    val time = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(tx.createdAt)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        timeFormat.format(date!!)
    } catch (e: Exception) {
        ""
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = if (isGiven) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp).combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = if (isGiven) 8.dp else 0.dp, bottomEnd = if (isGiven) 0.dp else 8.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1F2C34) else Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().background(if (isDark) Color(0xFF232D36) else Color(0xFFD1E3E1)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(text = "Added by $ownerName", fontSize = 11.sp, color = if (isDark) Color(0xFF8696A0) else Color(0xFF667781), fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = if (isGiven) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isDark) Color.White else Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "₹${String.format(Locale.getDefault(), "%,.0f", tx.amount)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                    
                    if (time.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = time, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Bottom))
                    }
                }

                if (!tx.description.isNullOrBlank()) {
                    Text(text = tx.description, fontSize = 13.sp, color = if (isDark) Color(0xFFD1D7DB) else Color(0xFF54656F), modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 8.dp))
                }
            }
        }
        
        // Show current due below the bubble
        Text(
            text = "₹${String.format(Locale.getDefault(), "%,.0f", abs(runningBalance))} ${if (runningBalance >= 0) "Due" else "Advance"}",
            fontSize = 12.sp,
            color = if (isDark) Color(0xFF8696A0) else Color(0xFF667781),
            modifier = Modifier.padding(top = 2.dp, start = if (isGiven) 0.dp else 4.dp, end = if (isGiven) 4.dp else 0.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: TransactionType,
    existingTransaction: Transaction? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit,
    onDelete: () -> Unit
) {
    var amount by remember { mutableStateOf(existingTransaction?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingTransaction != null) "Edit Transaction" else (if (type == TransactionType.CREDIT) "You Gave (Credit)" else "You Got (Received)")) },
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
                Text(if (existingTransaction != null) "Update" else "Confirm")
            }
        },
        dismissButton = {
            Row {
                if (existingTransaction != null) {
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
