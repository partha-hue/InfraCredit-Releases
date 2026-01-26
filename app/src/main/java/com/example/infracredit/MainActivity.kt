package com.example.infracredit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.infracredit.domain.repository.AuthRepository
import com.example.infracredit.ui.UpdateViewModel
import com.example.infracredit.ui.auth.LoginScreen
import com.example.infracredit.ui.auth.RegisterScreen
import com.example.infracredit.ui.calculator.CalculatorScreen
import com.example.infracredit.ui.components.UpdateDialog
import com.example.infracredit.ui.customer.AddCustomerScreen
import com.example.infracredit.ui.customer.ContactImportScreen
import com.example.infracredit.ui.customer.CustomerDetailScreen
import com.example.infracredit.ui.customer.CustomerListScreen
import com.example.infracredit.ui.customer.EditCustomerScreen
import com.example.infracredit.ui.customer.RecycleBinScreen
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
            var startDestination by remember { mutableStateOf<String?>(null) }
            val isDarkMode by themeManager.isDarkMode
            
            LaunchedEffect(Unit) {
                // Check for updates on app start from GitHub Raw URL
                updateViewModel.checkForUpdates("https://raw.githubusercontent.com/partha-hue/InfraCredit-Releases/main/version.json")

                startDestination = if (authRepository.isAuthenticated()) {
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
                    AppNavigation(destination)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
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
            AddCustomerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
            EditCustomerScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProfileEdit = { navController.navigate(Screen.ProfileEdit.route) },
                onNavigateToRecycleBin = { navController.navigate(Screen.RecycleBin.route) }
            )
        }
        composable(Screen.Calculator.route) {
            CalculatorScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Contacts.route) {
            ContactImportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.RecycleBin.route) {
            RecycleBinScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
