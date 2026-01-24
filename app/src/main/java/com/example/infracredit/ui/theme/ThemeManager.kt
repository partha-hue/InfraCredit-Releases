package com.example.infracredit.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor() {
    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> = _isDarkMode

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }
}
