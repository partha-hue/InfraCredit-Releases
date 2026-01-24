package com.example.infracredit.ui.dashboard

data class DashboardState(
    val isLoading: Boolean = false,
    val totalOutstanding: Double = 0.0,
    val todayCollection: Double = 0.0,
    val activeCustomers: Int = 0,
    val error: String? = null
)