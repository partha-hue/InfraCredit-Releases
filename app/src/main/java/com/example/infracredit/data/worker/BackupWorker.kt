package com.example.infracredit.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.infracredit.data.local.PreferenceManager
import com.example.infracredit.data.repository.BackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val preferenceManager: PreferenceManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val accountName = preferenceManager.googleAccountName.first() ?: return Result.failure()
        
        return try {
            val result = backupRepository.uploadBackup(accountName)
            if (result.isSuccess) {
                preferenceManager.saveLastBackupTime(System.currentTimeMillis())
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
