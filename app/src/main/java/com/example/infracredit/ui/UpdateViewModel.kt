package com.example.infracredit.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.BuildConfig
import com.example.infracredit.data.local.PreferenceManager
import com.example.infracredit.data.remote.UpdateManager
import com.example.infracredit.domain.model.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: UpdateManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    fun checkForUpdates(url: String) {
        viewModelScope.launch {
            val info = updateManager.getUpdateInfo(url)
            if (info != null && info.latestVersionCode > BuildConfig.VERSION_CODE) {
                val dismissedVersion = preferenceManager.dismissedVersionCode.first()
                if (info.latestVersionCode != dismissedVersion) {
                    updateInfo = info
                }
            }
        }
    }

    fun downloadUpdate() {
        updateInfo?.let {
            updateManager.downloadUpdate(it.apkUrl)
            updateInfo = null
        }
    }

    fun dismissDialog() {
        val currentInfo = updateInfo
        if (currentInfo != null) {
            viewModelScope.launch {
                preferenceManager.saveDismissedVersion(currentInfo.latestVersionCode)
                updateInfo = null
            }
        }
    }
}
