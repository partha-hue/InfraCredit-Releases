package com.example.infracredit.data.repository

import android.content.Context
import com.example.infracredit.data.local.PreferenceManager
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    private val DB_NAME = "infracredit.db"

    suspend fun uploadBackup(googleAccountName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            ).apply {
                selectedAccountName = googleAccountName
            }

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("InfraCredit").build()

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return@withContext Result.failure(Exception("Database file not found"))

            // Find existing backup file in AppData folder
            val existingFiles = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$DB_NAME'")
                .execute()

            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = DB_NAME
                parents = Collections.singletonList("appDataFolder")
            }

            val mediaContent = FileContent("application/x-sqlite3", dbFile)

            if (existingFiles.files.isNotEmpty()) {
                // Update existing file
                val fileId = existingFiles.files[0].id
                driveService.files().update(fileId, null, mediaContent).execute()
            } else {
                // Create new file
                driveService.files().create(fileMetadata, mediaContent).execute()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(googleAccountName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            ).apply {
                selectedAccountName = googleAccountName
            }

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("InfraCredit").build()

            val files = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$DB_NAME'")
                .execute()

            if (files.files.isEmpty()) return@withContext Result.failure(Exception("No backup found on Drive"))

            val fileId = files.files[0].id
            val dbFile = context.getDatabasePath(DB_NAME)
            
            FileOutputStream(dbFile).use { outputStream ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
