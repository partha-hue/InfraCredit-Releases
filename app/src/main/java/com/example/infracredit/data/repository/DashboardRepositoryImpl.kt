package com.example.infracredit.data.repository

import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.dto.DashboardSummaryDto
import com.example.infracredit.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val api: InfracreditApi
) : DashboardRepository {
    override suspend fun getSummary(): Result<DashboardSummaryDto> {
        return try {
            val response = api.getDashboardSummary()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
