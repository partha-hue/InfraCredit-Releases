package com.example.infracredit.data.repository

import android.content.Context
import com.example.infracredit.data.local.AppDatabase
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
    private val preferenceManager: PreferenceManager
) {
    private val DB_NAME = AppDatabase.DATABASE_NAME
    private val BACKUP_FILE_NAME = "infracredit_backup.zip"

    suspend fun uploadBackup(googleAccountName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("No local data found. Add a customer first."))
            }

            // Create a ZIP containing the DB and its journal files
            val zipFile = File(context.cacheDir, BACKUP_FILE_NAME)
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                val filesToBackup = listOf(DB_NAME, "$DB_NAME-shm", "$DB_NAME-wal")
                filesToBackup.forEach { fileName ->
                    val file = context.getDatabasePath(fileName)
                    if (file.exists()) {
                        zos.putNextEntry(ZipEntry(fileName))
                        FileInputStream(file).use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }

            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            ).apply { selectedAccountName = googleAccountName }

            val driveService = Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("InfraCredit").build()

            val existingFiles = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_FILE_NAME'")
                .execute()

            val mediaContent = FileContent("application/zip", zipFile)
            if (existingFiles.files.isNotEmpty()) {
                driveService.files().update(existingFiles.files[0].id, null, mediaContent).execute()
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
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(googleAccountName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            ).apply { selectedAccountName = googleAccountName }

            val driveService = Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("InfraCredit").build()

            val files = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_FILE_NAME'")
                .execute()

            if (files.files.isEmpty()) return@withContext Result.failure(Exception("No backup found on Drive"))

            val zipFile = File(context.cacheDir, BACKUP_FILE_NAME)
            FileOutputStream(zipFile).use { driveService.files().get(files.files[0].id).executeMediaAndDownloadTo(it) }

            // Extract ZIP to database folder
            java.util.zip.ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val outFile = context.getDatabasePath(entry.name)
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(outFile).use { input.copyTo(it) }
                    }
                }
            }
            
            zipFile.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
