package com.example.eyeguard.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.eyeguard.data.preferences.PrefsKeys
import com.example.eyeguard.domain.models.AppThemeMode
import com.example.eyeguard.domain.models.BreakEndAlertType
import com.example.eyeguard.domain.models.EyeGuardSettings
import com.example.eyeguard.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val settings: Flow<EyeGuardSettings> =
        dataStore.data
            .map { prefs ->
                EyeGuardSettings(
                    enabled = prefs[PrefsKeys.ENABLED] ?: false,
                    workIntervalMinutes = prefs[PrefsKeys.WORK_INTERVAL_MINUTES]
                        ?: EyeGuardSettings.DEFAULT_WORK_MINUTES,
                    breakDurationSeconds = prefs[PrefsKeys.BREAK_DURATION_SECONDS]
                        ?: EyeGuardSettings.DEFAULT_BREAK_SECONDS,
                    breakEndAlertType = prefs[PrefsKeys.BREAK_END_ALERT]
                        ?.let { name ->
                            runCatching { BreakEndAlertType.valueOf(name) }.getOrNull()
                        }
                        ?: EyeGuardSettings.DEFAULT_BREAK_END_ALERT,
                    themeMode = prefs[PrefsKeys.THEME_MODE]
                        ?.let { name ->
                            runCatching { AppThemeMode.valueOf(name) }.getOrNull()
                        }
                        ?: AppThemeMode.DARK,
                    languageCode = prefs[PrefsKeys.LANGUAGE_CODE] ?: EyeGuardSettings.DEFAULT_LANGUAGE
                )
            }
            .catch {
                emit(EyeGuardSettings())
            }

    override suspend fun update(transform: (EyeGuardSettings) -> EyeGuardSettings) {
        dataStore.edit { prefs ->
            val current = EyeGuardSettings(
                enabled = prefs[PrefsKeys.ENABLED] ?: false,
                workIntervalMinutes = prefs[PrefsKeys.WORK_INTERVAL_MINUTES]
                    ?: EyeGuardSettings.DEFAULT_WORK_MINUTES,
                breakDurationSeconds = prefs[PrefsKeys.BREAK_DURATION_SECONDS]
                    ?: EyeGuardSettings.DEFAULT_BREAK_SECONDS,
                breakEndAlertType = prefs[PrefsKeys.BREAK_END_ALERT]
                    ?.let { name ->
                        runCatching { BreakEndAlertType.valueOf(name) }.getOrNull()
                    }
                    ?: EyeGuardSettings.DEFAULT_BREAK_END_ALERT,
                themeMode = prefs[PrefsKeys.THEME_MODE]
                    ?.let { name ->
                        runCatching { AppThemeMode.valueOf(name) }.getOrNull()
                    }
                    ?: AppThemeMode.DARK,
                languageCode = prefs[PrefsKeys.LANGUAGE_CODE] ?: EyeGuardSettings.DEFAULT_LANGUAGE
            )

            val updated = transform(current).let {
                it.copy(
                    workIntervalMinutes = it.workIntervalMinutes.coerceIn(1, 180),
                    breakDurationSeconds = it.breakDurationSeconds.coerceIn(5, 300)
                )
            }

            prefs[PrefsKeys.ENABLED] = updated.enabled
            prefs[PrefsKeys.WORK_INTERVAL_MINUTES] = updated.workIntervalMinutes
            prefs[PrefsKeys.BREAK_DURATION_SECONDS] = updated.breakDurationSeconds
            prefs[PrefsKeys.BREAK_END_ALERT] = updated.breakEndAlertType.name
            prefs[PrefsKeys.THEME_MODE] = updated.themeMode.name
            prefs[PrefsKeys.LANGUAGE_CODE] = updated.languageCode
        }
    }
}
