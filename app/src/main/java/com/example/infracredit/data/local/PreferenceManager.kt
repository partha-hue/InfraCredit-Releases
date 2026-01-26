package com.example.infracredit.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DISMISSED_VERSION_CODE = intPreferencesKey("dismissed_version_code")
    }

    val dismissedVersionCode: Flow<Int> = context.dataStore.data.map { 
        it[DISMISSED_VERSION_CODE] ?: -1 
    }

    suspend fun saveDismissedVersion(versionCode: Int) {
        context.dataStore.edit { prefs ->
            prefs[DISMISSED_VERSION_CODE] = versionCode
        }
    }
}
