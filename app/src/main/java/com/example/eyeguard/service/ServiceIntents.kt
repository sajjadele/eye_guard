package com.example.eyeguard.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.eyeguard.domain.models.EyeGuardSettings

fun Context.startEyeProtection(settings: EyeGuardSettings) {
    val intent = Intent(this, EyeProtectionService::class.java)
        .setAction(EyeProtectionService.ACTION_START)
        .putExtra(
            EyeProtectionService.EXTRA_WORK_MINUTES,
            settings.workIntervalMinutes
        )
        .putExtra(
            EyeProtectionService.EXTRA_BREAK_SECONDS,
            settings.breakDurationSeconds
        )

    ContextCompat.startForegroundService(this, intent)
}

fun Context.stopEyeProtection() {
    val intent = Intent(this, EyeProtectionService::class.java)
        .setAction(EyeProtectionService.ACTION_STOP)

    ContextCompat.startForegroundService(this, intent)
}

/** Debug-only helper: schedules the first break after a short fixed interval (10s). */
fun Context.startEyeProtectionTest(settings: EyeGuardSettings) {
    val intent = Intent(this, EyeProtectionService::class.java)
        .setAction(EyeProtectionService.ACTION_START)
        .putExtra(
            EyeProtectionService.EXTRA_WORK_MINUTES,
            settings.workIntervalMinutes
        )
        .putExtra(
            EyeProtectionService.EXTRA_BREAK_SECONDS,
            settings.breakDurationSeconds
        )
        .putExtra(
            EyeProtectionService.EXTRA_TEST_INTERVAL_SECONDS,
            10
        )

    ContextCompat.startForegroundService(this, intent)
}
