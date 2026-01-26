package com.example.infracredit.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infracredit.data.remote.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: UpdateManager
) : ViewModel() {

    var updateUrl by mutableStateOf<String?>(null)
        private set

    fun checkForUpdates(url: String) {
        viewModelScope.launch {
            updateUrl = updateManager.checkForUpdate(url)
        }
    }

    fun downloadUpdate() {
        updateUrl?.let {
            updateManager.downloadUpdate(it)
            updateUrl = null
        }
    }

    fun dismissDialog() {
        updateUrl = null
    }
}
