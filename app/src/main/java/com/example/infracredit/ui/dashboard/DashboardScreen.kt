package com.example.infracredit.ui.dashboard

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.infracredit.R
import com.example.infracredit.domain.model.Customer
import com.example.infracredit.ui.customer.CustomerViewModel
import com.example.infracredit.ui.settings.SettingsViewModel
import java.util.Locale

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
    customerViewModel: CustomerViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val dashState = dashboardViewModel.state.value
    val custState = customerViewModel.listState.value
    val profileState = settingsViewModel.profileState.value
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboardData()
        customerViewModel.getCustomers()
        settingsViewModel.loadProfile()
    }

    val netBalance = dashState.totalOutstanding - dashState.todayCollection

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.animateContentSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF0054A6), Color(0xFF00B4D8))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "InfraCredit",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = profileState.profile?.businessName ?: "Premium Ledger",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToCalculator,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Rounded.Calculate, contentDescription = "Calculator", modifier = Modifier.size(20.dp))
                    }
                    
                    IconButton(
                        onClick = onNavigateToContacts,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Rounded.ContactPhone, contentDescription = "Import Contacts", modifier = Modifier.size(20.dp))
                    }

                    val profile = profileState.profile
                    if (profile?.profilePic != null) {
                        val bitmap = remember(profile.profilePic) {
                            try {
                                val imageBytes = Base64.decode(profile.profilePic, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { showProfileDialog = true }
                                    .shadow(1.dp, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            IconButton(onClick = { showProfileDialog = true }, modifier = Modifier.padding(end = 4.dp)) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(32.dp))
                            }
                        }
                    } else {
                        IconButton(onClick = { showProfileDialog = true }, modifier = Modifier.padding(end = 4.dp)) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(32.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Net Balance Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (netBalance >= 0) {
                                    listOf(Color(0xFF0054A6), Color(0xFF003366))
                                } else {
                                    listOf(Color(0xFFB71C1C), Color(0xFF7F0000))
                                }
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (netBalance >= 0) "NET RECEIVABLE BALANCE" else "NET PAYABLE BALANCE",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = if (netBalance >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹ ${String.format(Locale.getDefault(), "%,.2f", kotlin.math.abs(netBalance))}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Dual Summary Cards Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernSummaryCard(
                    label = "You'll Get",
                    amount = dashState.totalOutstanding,
                    icon = Icons.Rounded.ArrowDownward,
                    color = Color(0xFF2E7D32),
                    backgroundColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )
                ModernSummaryCard(
                    label = "You'll Give",
                    amount = dashState.todayCollection,
                    icon = Icons.Rounded.ArrowUpward,
                    color = Color(0xFFC62828),
                    backgroundColor = Color(0xFFFFEBEE),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar & Filter Section
            AdvancedSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            AdvancedFilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // Header for customer list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customers & Ledgers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                IconButton(
                    onClick = { dashboardViewModel.loadDashboardData(); customerViewModel.getCustomers() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF0054A6),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Customer List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (custState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF0054A6), strokeWidth = 3.dp)
                        }
                    }
                } else if (custState.error != null && custState.customers.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(text = "Offline Mode - Displaying Cached Data", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    val filteredCustomers = custState.customers.filter {
                        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || (it.phone?.contains(searchQuery) ?: false)
                        val matchesFilter = when (selectedFilter) {
                            "All" -> true
                            "Credit Due" -> it.totalDue > 0
                            "You Owe" -> it.totalDue < 0
                            "Settled" -> it.totalDue == 0.0
                            else -> true
                        }
                        matchesSearch && matchesFilter
                    }
                    
                    if (filteredCustomers.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Rounded.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
                                Spacer(Modifier.height(12.dp))
                                Text("No matching customers found", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        items(filteredCustomers, key = { it.id }) { customer ->
                            PremiumCustomerItem(customer, onNavigateToDetail)
                        }
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            confirmButton = {
                Button(
                    onClick = { showProfileDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0054A6))
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileDialog = false
                    dashboardViewModel.logout { onNavigateToSettings() }
                }) {
                    Text("Logout", color = Color.Red)
                }
            },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Business Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    val profile = profileState.profile
                    if (profile?.profilePic != null) {
                        val bitmap = remember(profile.profilePic) {
                            try {
                                val imageBytes = Base64.decode(profile.profilePic, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(Color(0xFF0054A6).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(44.dp), tint = Color(0xFF0054A6))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(text = profile?.fullName ?: "Merchant Partner", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(text = "📞 ${profile?.phone ?: "Not available"}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!profile?.businessName.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF0054A6).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = profile!!.businessName,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0054A6)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ModernSummaryCard(label: String, amount: Double, icon: ImageVector, color: Color, backgroundColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search by name or phone number...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF0054A6)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = Color.Gray)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color(0xFF0054A6),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        ),
        singleLine = true
    )
}

@Composable
fun AdvancedFilterChips(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All", "Credit Due", "You Owe", "Settled")
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) Color(0xFF0054A6) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PremiumCustomerItem(customer: Customer, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick(customer.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isCredit = customer.totalDue > 0
            val isDebit = customer.totalDue < 0
            
            val avatarColor = when {
                isCredit -> Color(0xFFC62828)
                isDebit -> Color(0xFF2E7D32)
                else -> Color(0xFF0054A6)
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(avatarColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1).uppercase(),
                    color = avatarColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (!customer.phone.isNullOrBlank()) customer.phone else "No contact number",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", kotlin.math.abs(customer.totalDue))}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = when {
                        isCredit -> Color(0xFFC62828)
                        isDebit -> Color(0xFF2E7D32)
                        else -> Color.Gray
                    }
                )
                
                Surface(
                    color = when {
                        isCredit -> Color(0xFFFFEBEE)
                        isDebit -> Color(0xFFE8F5E9)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = when {
                            isCredit -> "YOU'LL GET"
                            isDebit -> "YOU'LL GIVE"
                            else -> "SETTLED"
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCredit -> Color(0xFFC62828)
                            isDebit -> Color(0xFF2E7D32)
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
