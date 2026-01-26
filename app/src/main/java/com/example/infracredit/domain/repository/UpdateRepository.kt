package com.example.infracredit.domain.repository

import com.example.infracredit.domain.model.UpdateInfo

interface UpdateRepository {
    suspend fun checkForUpdate(url: String): UpdateInfo
}
