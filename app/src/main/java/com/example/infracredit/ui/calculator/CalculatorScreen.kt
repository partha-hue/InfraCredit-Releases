package com.example.infracredit.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onNavigateBack: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }

    val buttons = listOf(
        "C", "÷", "×", "⌫",
        "7", "8", "9", "-",
        "4", "5", "6", "+",
        "1", "2", "3", "=",
        "%", "0", ".", ""
    )

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
                    color = Color.Gray,
                    textAlign = TextAlign.End
                )
                Text(
                    text = result,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End
                )
            }

            // Buttons Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(buttons) { btn ->
                    if (btn.isNotEmpty()) {
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
                                        result = try {
                                            evaluateExpression(expression).toString()
                                        } catch (e: Exception) {
                                            "Error"
                                        }
                                    }
                                }
                                else -> {
                                    expression += btn
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(70.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CalcButton(text: String, onClick: () -> Unit) {
    val isOperator = text in listOf("÷", "×", "-", "+", "=")
    val isSpecial = text in listOf("C", "⌫", "%")

    Surface(
        modifier = Modifier
            .size(70.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = when {
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (text == "=") Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun evaluateExpression(expression: String): Double {
    val exp = expression.replace("×", "*").replace("÷", "/")
    // Very simple evaluator for +/-/*// (Production app would use a library)
    return try {
        val parts = exp.split(Regex("(?=[-+*/])|(?<=[-+*/])"))
        if (parts.size < 3) return parts[0].toDoubleOrNull() ?: 0.0
        
        var res = parts[0].toDouble()
        var i = 1
        while (i < parts.size) {
            val op = parts[i]
            val nextVal = parts[i+1].toDouble()
            when (op) {
                "+" -> res += nextVal
                "-" -> res -= nextVal
                "*" -> res *= nextVal
                "/" -> res /= nextVal
            }
            i += 2
        }
        res
    } catch (e: Exception) {
        0.0
    }
}
