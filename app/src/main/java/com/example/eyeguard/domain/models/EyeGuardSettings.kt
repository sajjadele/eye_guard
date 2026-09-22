package com.example.eyeguard.domain.models

enum class BreakEndAlertType {
    SOUND,
    VIBRATION,
    BOTH,
    NONE
}

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class EyeGuardSettings(
    val enabled: Boolean = false,
    val workIntervalMinutes: Int = DEFAULT_WORK_MINUTES,
    val breakDurationSeconds: Int = DEFAULT_BREAK_SECONDS,
    val breakEndAlertType: BreakEndAlertType = DEFAULT_BREAK_END_ALERT,
    val vocabularyEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.DARK
) {
    companion object {
        const val DEFAULT_WORK_MINUTES = 20
        const val DEFAULT_BREAK_SECONDS = 30

        val DEFAULT_BREAK_END_ALERT = BreakEndAlertType.SOUND

        val WORK_INTERVALS = listOf(20, 30, 60)
        val BREAK_DURATIONS = listOf(30, 60, 90)
    }
}
