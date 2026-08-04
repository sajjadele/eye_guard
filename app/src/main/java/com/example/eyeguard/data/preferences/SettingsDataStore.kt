package com.example.eyeguard.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "eyeguard_settings")

object PrefsKeys {
    val ENABLED = booleanPreferencesKey("enabled")
    val WORK_INTERVAL_MINUTES = intPreferencesKey("work_interval_minutes")
    val BREAK_DURATION_SECONDS = intPreferencesKey("break_duration_seconds")
    val BREAK_END_ALERT = stringPreferencesKey("break_end_alert")
}
