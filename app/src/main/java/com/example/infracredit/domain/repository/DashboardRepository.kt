package com.example.infracredit.domain.repository

import com.example.infracredit.data.remote.dto.DashboardSummaryDto

interface DashboardRepository {
    suspend fun getSummary(): Result<DashboardSummaryDto>
}