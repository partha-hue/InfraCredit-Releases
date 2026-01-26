package com.example.infracredit.data.remote

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.infracredit.BuildConfig
import com.example.infracredit.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: UpdateRepository
) {
    suspend fun checkForUpdate(url: String): String? {
        return try {
            val updateInfo = repository.checkForUpdate(url)
            if (updateInfo.latestVersionCode > BuildConfig.VERSION_CODE) {
                updateInfo.apkUrl
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun downloadUpdate(apkUrl: String) {
        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("App Update")
            .setDescription("Downloading latest version of InfraCredit")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
