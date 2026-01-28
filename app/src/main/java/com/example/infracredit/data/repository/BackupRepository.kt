package com.example.infracredit.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.infracredit.data.local.AppDatabase
import com.example.infracredit.data.local.PreferenceManager
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Collections
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager,
    private val database: AppDatabase
) {
    private val TAG = "BackupRepository"
    private val DB_NAME = AppDatabase.DATABASE_NAME
    private val BACKUP_FILE_NAME = "infracredit_backup.zip"

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    suspend fun uploadBackup(googleAccountName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (googleAccountName.isEmpty()) {
                return@withContext Result.failure(Exception("Google account name is empty. Please sign in again."))
            }

            if (!isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No internet connection. Please check your network."))
            }

            // Force a checkpoint to flush WAL files
            try {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            } catch (e: Exception) {
                Log.e(TAG, "WAL checkpoint failed", e)
            }

            val filesToBackup = listOf(DB_NAME, "$DB_NAME-shm", "$DB_NAME-wal", "$DB_NAME-journal")
            val existingFiles = filesToBackup.map { context.getDatabasePath(it) }.filter { it.exists() }

            if (existingFiles.isEmpty()) {
                return@withContext Result.failure(Exception("No local data found to backup."))
            }

            val zipFile = File(context.cacheDir, BACKUP_FILE_NAME)
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                existingFiles.forEach { file ->
                    zos.putNextEntry(ZipEntry(file.name))
                    FileInputStream(file).use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }

            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            ).apply { selectedAccountName = googleAccountName }

            val driveService = Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("InfraCredit")
                .build()

            val existingDriveFiles = try {
                driveService.files().list()
                    .setSpaces("appDataFolder")
                    .setQ("name = '$BACKUP_FILE_NAME'")
                    .execute()
            } catch (e: UserRecoverableAuthIOException) {
                // Return as a specific failure so UI can handle it
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Drive access failed", e)
                val msg = e.message ?: ""
                when {
                    msg.contains("403") -> throw Exception("Access Denied (403): Check if Google Drive API is enabled and SHA-1 is correct.")
                    msg.contains("401") -> throw Exception("Auth Failed (401): Please sign out and sign in again.")
                    else -> throw e
                }
            }

            val mediaContent = FileContent("application/zip", zipFile)
            if (existingDriveFiles.files != null && existingDriveFiles.files.isNotEmpty()) {
                driveService.files().update(existingDriveFiles.files[0].id, null, mediaContent).execute()
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = BACKUP_FILE_NAME
                    parents = Collections.singletonList("appDataFolder")
                }
                driveService.files().create(fileMetadata, mediaContent).execute()
            }

            zipFile.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(googleAccountName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (googleAccountName.isEmpty()) return@withContext Result.failure(Exception("Account name is empty"))

            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            ).apply { selectedAccountName = googleAccountName }

            val driveService = Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("InfraCredit").build()

            val files = try {
                driveService.files().list()
                    .setSpaces("appDataFolder")
                    .setQ("name = '$BACKUP_FILE_NAME'")
                    .execute()
            } catch (e: UserRecoverableAuthIOException) {
                return@withContext Result.failure(e)
            }

            if (files.files == null || files.files.isEmpty()) {
                return@withContext Result.failure(Exception("No backup found on Google Drive."))
            }

            val zipFile = File(context.cacheDir, BACKUP_FILE_NAME)
            FileOutputStream(zipFile).use { driveService.files().get(files.files[0].id).executeMediaAndDownloadTo(it) }

            database.close()
            java.util.zip.ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val outFile = context.getDatabasePath(entry.name)
                    outFile.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(outFile).use { input.copyTo(it) }
                    }
                }
            }
            zipFile.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            Result.failure(e)
        }
    }
}
