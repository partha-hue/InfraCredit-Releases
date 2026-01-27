package com.example.infracredit.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        private val GOOGLE_ACCOUNT_NAME = stringPreferencesKey("google_account_name")
    }

    val dismissedVersionCode: Flow<Int> = context.dataStore.data.map { 
        it[DISMISSED_VERSION_CODE] ?: -1 
    }

    val lastBackupTime: Flow<Long> = context.dataStore.data.map {
        it[LAST_BACKUP_TIME] ?: 0L
    }

    val googleAccountName: Flow<String?> = context.dataStore.data.map {
        it[GOOGLE_ACCOUNT_NAME]
    }

    suspend fun saveDismissedVersion(versionCode: Int) {
        context.dataStore.edit { prefs ->
            prefs[DISMISSED_VERSION_CODE] = versionCode
        }
    }

    suspend fun saveLastBackupTime(timeMillis: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_BACKUP_TIME] = timeMillis
        }
    }

    suspend fun saveGoogleAccountName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[GOOGLE_ACCOUNT_NAME] = name
        }
    }
}
