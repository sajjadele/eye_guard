package com.example.eyeguard.domain.models

data class EyeGuardSettings(
    val enabled: Boolean = false,
    val workIntervalMinutes: Int = DEFAULT_WORK_MINUTES,
    val breakDurationSeconds: Int = DEFAULT_BREAK_SECONDS
) {
    companion object {
        const val DEFAULT_WORK_MINUTES = 20
        const val DEFAULT_BREAK_SECONDS = 30

        val WORK_INTERVALS = listOf(20, 30, 60)
        val BREAK_DURATIONS = listOf(30, 60, 90)
    }
}
