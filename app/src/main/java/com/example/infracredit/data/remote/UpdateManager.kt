package com.example.infracredit.data.remote

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.infracredit.BuildConfig
import com.example.infracredit.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: UpdateRepository
) {
    suspend fun checkForUpdate(url: String): String? {
        return try {
            val cacheBusterUrl = "$url?t=${System.currentTimeMillis()}"
            val updateInfo = repository.checkForUpdate(cacheBusterUrl)
            
            if (updateInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
                val apkUrlWithCacheBuster = if (updateInfo.apkUrl.contains("?")) {
                    "${updateInfo.apkUrl}&t=${System.currentTimeMillis()}"
                } else {
                    "${updateInfo.apkUrl}?t=${System.currentTimeMillis()}"
                }
                apkUrlWithCacheBuster
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadUpdate(apkUrl: String) {
        // Delete old update file if exists to prevent DownloadManager from renaming it (e.g. update-1.apk)
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("InfraCredit Update")
            .setDescription("Downloading latest version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
