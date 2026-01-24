package com.example.infracredit.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.domain.model.TransactionType
import com.example.infracredit.ui.customer.CustomerViewModel
import com.example.infracredit.ui.customer.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateBack: () -> Unit,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val buttons = listOf(
        "C", "÷", "×", "⌫",
        "7", "8", "9", "-",
        "4", "5", "6", "+",
        "1", "2", "3", "=",
        "%", "0", ".", "ADD"
    )

    LaunchedEffect(Unit) {
        customerViewModel.getCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expression,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
                Text(
                    text = result,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )
            }

            // Buttons Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gridItems(buttons) { btn ->
                    CalcButton(btn) {
                        when (btn) {
                            "C" -> {
                                expression = ""
                                result = "0"
                            }
                            "⌫" -> {
                                if (expression.isNotEmpty()) expression = expression.dropLast(1)
                            }
                            "=" -> {
                                if (expression.isNotEmpty()) {
                                    result = evaluateExpression(expression)
                                }
                            }
                            "ADD" -> {
                                if (result != "0" && result != "Error") {
                                    showCustomerPicker = true
                                }
                            }
                            else -> {
                                expression += btn
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomerPicker) {
        CustomerPickerDialog(
            customers = customerViewModel.listState.value.customers,
            onDismiss = { showCustomerPicker = false },
            onSelect = { customer ->
                val amountValue = result.replace(",", "").toDoubleOrNull() ?: 0.0
                if (amountValue > 0) {
                    transactionViewModel.addTransaction(amountValue, TransactionType.CREDIT, "Added from Calculator", customerId = customer.id)
                }
                showCustomerPicker = false
                onNavigateBack()
            }
        )
    }
}

@Composable
fun CalcButton(text: String, onClick: () -> Unit) {
    val isOperator = text in listOf("÷", "×", "-", "+", "=", "ADD")
    val isSpecial = text in listOf("C", "⌫", "%")

    Surface(
        modifier = Modifier
            .size(70.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = when {
            text == "ADD" -> Color(0xFF00A884)
            text == "=" -> MaterialTheme.colorScheme.primary
            isOperator -> MaterialTheme.colorScheme.primaryContainer
            isSpecial -> Color.LightGray.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surface
        },
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = if (text == "ADD") 14.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (text == "=" || text == "ADD") Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPickerDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onSelect: (Customer) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxHeight(0.6f)) {
            Text(
                "Add Amount to Customer",
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            LazyColumn {
                listItems(customers) { customer ->
                    ListItem(
                        modifier = Modifier.clickable { onSelect(customer) },
                        headlineContent = { Text(customer.name) },
                        supportingContent = { Text("Balance: ₹${customer.totalDue}") },
                        leadingContent = { Icon(Icons.Default.Person, null) }
                    )
                }
            }
        }
    }
}

private fun evaluateExpression(expression: String): String {
    if (expression.isEmpty()) return "0"
    
    val exp = expression.replace("×", "*").replace("÷", "/")
    return try {
        // Simple regex to split but keep delimiters
        val parts = exp.split(Regex("(?<=[-+*/])|(?=[-+*/])")).filter { it.isNotBlank() }
        if (parts.isEmpty()) return "0"
        
        var res = parts[0].toDoubleOrNull() ?: 0.0
        var i = 1
        while (i < parts.size) {
            val op = parts[i]
            if (i + 1 < parts.size) {
                val nextVal = parts[i + 1].toDoubleOrNull() ?: 0.0
                when (op) {
                    "+" -> res += nextVal
                    "-" -> res -= nextVal
                    "*" -> res *= nextVal
                    "/" -> if (nextVal != 0.0) res /= nextVal else return "Error"
                }
            }
            i += 2
        }
        if (res % 1 == 0.0) res.toInt().toString() else String.format("%.2f", res)
    } catch (e: Exception) {
        "Error"
    }
}
