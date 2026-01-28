package com.example.infracredit.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
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
        val accountName = preferenceManager.googleAccountName.first() ?: return Result.failure(
            Data.Builder().putString("error", "No Google account linked").build()
        )
        
        return try {
            val result = backupRepository.uploadBackup(accountName)
            if (result.isSuccess) {
                preferenceManager.saveLastBackupTime(System.currentTimeMillis())
                Result.success()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                // Pass the specific error message to the UI
                Result.failure(Data.Builder().putString("error", errorMsg).build())
            }
        } catch (e: Exception) {
            Result.failure(Data.Builder().putString("error", e.message).build())
        }
    }
}
