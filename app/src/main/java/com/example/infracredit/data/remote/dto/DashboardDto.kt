package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummaryDto(
    val totalOutstanding: Double,
    val todayCollection: Double,
    val activeCustomers: Int
)