package com.example.infracredit.data.repository

import com.example.infracredit.data.remote.UpdateService
import com.example.infracredit.domain.model.UpdateInfo
import com.example.infracredit.domain.repository.UpdateRepository
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    private val updateService: UpdateService
) : UpdateRepository {
    override suspend fun checkForUpdate(url: String): UpdateInfo {
        return updateService.getUpdateInfo(url)
    }
}
