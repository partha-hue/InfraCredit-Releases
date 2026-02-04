package com.example.infracredit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.infracredit.domain.repository.AuthRepository
import com.example.infracredit.ui.UpdateViewModel
import com.example.infracredit.ui.auth.LoginScreen
import com.example.infracredit.ui.auth.RegisterScreen
import com.example.infracredit.ui.calculator.CalculatorScreen
import com.example.infracredit.ui.components.UpdateDialog
import com.example.infracredit.ui.customer.*
import com.example.infracredit.ui.dashboard.DashboardScreen
import com.example.infracredit.ui.navigation.Screen
import com.example.infracredit.ui.settings.ProfileEditScreen
import com.example.infracredit.ui.settings.SettingsScreen
import com.example.infracredit.ui.theme.InfraCreditTheme
import com.example.infracredit.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val updateViewModel: UpdateViewModel = hiltViewModel()
            val customerViewModel: CustomerViewModel = hiltViewModel()
            var startDestination by remember { mutableStateOf<String?>(null) }
            val isDarkMode by themeManager.isDarkMode
            
            LaunchedEffect(Unit) {
                updateViewModel.checkForUpdates("https://raw.githubusercontent.com/partha-hue/InfraCredit-Releases/main/version.json")
                startDestination = if (authRepository.isAuthenticated()) {
                    customerViewModel.getCustomers(false)
                    Screen.Dashboard.route
                } else {
                    Screen.Login.route
                }
            }

            InfraCreditTheme(darkTheme = isDarkMode) {
                updateViewModel.updateInfo?.let {
                    UpdateDialog(
                        onDismiss = { updateViewModel.dismissDialog() },
                        onUpdateClick = { updateViewModel.downloadUpdate() }
                    )
                }

                startDestination?.let { destination ->
                    MainContainer(destination)
                }
            }
        }
    }
}

@Composable
fun MainContainer(startDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf(
        Screen.Dashboard.route,
        Screen.Calculator.route,
        Screen.Contacts.route,
        Screen.Settings.route
    )

    val showBottomBar = currentDestination?.route in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        Quadruple("Customers", Icons.Default.People, Screen.Dashboard.route),
                        Quadruple("Calculator", Icons.Outlined.Calculate, Screen.Calculator.route),
                        Quadruple("Contacts", Icons.Outlined.Contacts, Screen.Contacts.route),
                        Quadruple("Settings", Icons.Default.Settings, Screen.Settings.route)
                    )

                    items.forEach { (label, icon, route) ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0054A6),
                                selectedTextColor = Color(0xFF0054A6),
                                indicatorColor = Color(0xFF0054A6).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAddCustomer = { navController.navigate(Screen.AddCustomer.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.CustomerList.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.CustomerDetail.createRoute(id)) },
                    onNavigateToCalculator = { navController.navigate(Screen.Calculator.route) },
                    onNavigateToContacts = { navController.navigate(Screen.Contacts.route) }
                )
            }
            composable(Screen.CustomerList.route) {
                CustomerListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { id -> navController.navigate(Screen.CustomerDetail.createRoute(id)) },
                    onNavigateToAdd = { navController.navigate(Screen.AddCustomer.route) }
                )
            }
            composable(Screen.AddCustomer.route) {
                AddCustomerScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.CustomerDetail.route,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType })
            ) {
                CustomerDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.EditCustomer.createRoute(id)) }
                )
            }
            composable(
                route = Screen.EditCustomer.route,
                arguments = listOf(navArgument("customerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                EditCustomerScreen(customerId = customerId, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    onNavigateToProfileEdit = { navController.navigate(Screen.ProfileEdit.route) },
                    onNavigateToRecycleBin = { navController.navigate(Screen.RecycleBin.route) }
                )
            }
            composable(Screen.Calculator.route) {
                CalculatorScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Contacts.route) {
                ContactImportScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.ProfileEdit.route) {
                ProfileEditScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.RecycleBin.route) {
                RecycleBinScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

data class Quadruple<A, B, C>(val first: A, val second: B, val third: C)
