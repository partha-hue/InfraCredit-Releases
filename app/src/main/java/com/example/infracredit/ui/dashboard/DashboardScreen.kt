package com.example.infracredit.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.ui.customer.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToContacts: () -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val dashState = dashboardViewModel.state.value
    val custState = customerViewModel.listState.value
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboardData()
        customerViewModel.getCustomers()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("InfraCredit", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            WhatsAppBottomNavigation(
                onCustomersClick = { /* Already here */ },
                onCalculatorClick = onNavigateToCalculator,
                onContactsClick = onNavigateToContacts,
                onProfileClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ADD CUSTOMER", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Summary Section
            SummarySection(dashState)

            // Search Bar
            WhatsAppSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            // Filters
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // Customer List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (custState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    val filteredCustomers = custState.customers.filter {
                        it.name.contains(searchQuery, ignoreCase = true) &&
                        (selectedFilter == "All" || (selectedFilter == "Credit Due" && it.totalDue > 0))
                    }
                    
                    items(filteredCustomers) { customer ->
                        WhatsAppCustomerItem(customer, onNavigateToDetail)
                    }
                }
            }
        }
    }
}

@Composable
fun SummarySection(state: DashboardState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            label = "Credit",
            amount = state.totalOutstanding.toString(),
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFD32F2F),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Payment",
            amount = state.todayCollection.toString(),
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF388E3C),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(label: String, amount: String, containerColor: Color, contentColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = contentColor.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
            Text("₹ $amount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search customers", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Transparent,
        ),
        singleLine = true
    )
}

@Composable
fun FilterChips(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All", "Credit Due", "Clear")
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WhatsAppCustomerItem(customer: Customer, onClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(customer.id) },
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9ECEF))
                    .clickable { /* Profile Preview */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Last updated: ${customer.createdAt.split("T")[0]}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                val isCredit = customer.totalDue > 0
                Text(
                    text = "₹ ${kotlin.math.abs(customer.totalDue)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isCredit) Color(0xFFD32F2F) else Color(0xFF388E3C)
                )
                Text(
                    text = if (isCredit) "Credit" else "Payment",
                    fontSize = 10.sp,
                    color = if (isCredit) Color(0xFFD32F2F).copy(alpha = 0.7f) else Color(0xFF388E3C).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun WhatsAppBottomNavigation(
    onCustomersClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    onContactsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("Customers", Icons.Default.Person, onCustomersClick),
            Triple("Calculator", Icons.Outlined.Calculate, onCalculatorClick),
            Triple("Contacts", Icons.Outlined.Contacts, onContactsClick),
            Triple("Profile", Icons.Outlined.Person, onProfileClick)
        )
        
        items.forEach { (label, icon, onClick) ->
            NavigationBarItem(
                selected = label == "Customers",
                onClick = onClick,
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}