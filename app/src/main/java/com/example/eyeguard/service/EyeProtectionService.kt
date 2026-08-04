package com.example.eyeguard.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.eyeguard.EyeGuardContainer
import com.example.eyeguard.R
import com.example.eyeguard.presentation.MainActivity
import com.example.eyeguard.presentation.theme.EyeGuardTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class EyeProtectionService : LifecycleService() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SHOW_BREAK = "ACTION_SHOW_BREAK"
        const val ACTION_CONTINUE = "ACTION_CONTINUE"

        const val EXTRA_WORK_MINUTES = "EXTRA_WORK_MINUTES"
        const val EXTRA_BREAK_SECONDS = "EXTRA_BREAK_SECONDS"

        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "eye_guard_channel"
    }

    private val repository = EyeGuardContainer.settingsRepository

    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val alarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private val powerManager by lazy {
        getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private var overlayView: ComposeView? = null
    private var breakJob: Job? = null
    private var breakWakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val result = super.onStartCommand(intent, flags, startId)

        startForegroundWithNotification()

        when (intent?.action) {
            ACTION_START -> {
                hideOverlay()

                val workIntervalMinutes = intent.getIntExtra(EXTRA_WORK_MINUTES, -1)
                val breakDurationSeconds = intent.getIntExtra(EXTRA_BREAK_SECONDS, -1)

                lifecycleScope.launch {
                    if (workIntervalMinutes > 0 && breakDurationSeconds > 0) {
                        repository.update {
                            it.copy(
                                enabled = true,
                                workIntervalMinutes = workIntervalMinutes,
                                breakDurationSeconds = breakDurationSeconds
                            )
                        }
                    } else {
                        repository.update {
                            it.copy(enabled = true)
                        }
                    }

                    val settings = repository.settings.first()
                    scheduleBreakAlarm(settings.workIntervalMinutes)
                }
            }

            ACTION_SHOW_BREAK -> {
                lifecycleScope.launch {
                    val settings = repository.settings.first()

                    if (settings.enabled) {
                        showBreakOverlay(settings.breakDurationSeconds)
                    } else {
                        stopProtection()
                    }
                }
            }

            ACTION_CONTINUE -> {
                handleContinue()
            }

            ACTION_STOP -> {
                stopProtection()
            }

            else -> {
                lifecycleScope.launch {
                    val settings = repository.settings.first()

                    if (settings.enabled) {
                        scheduleBreakAlarm(settings.workIntervalMinutes)
                    } else {
                        stopProtection()
                    }
                }
            }
        }

        return result
    }

    override fun onDestroy() {
        cancelAlarm()
        hideOverlay()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun startForegroundWithNotification() {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } catch (throwable: Throwable) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification()
                )
            } catch (_: Throwable) {
                // The app should not crash if notification cannot be shown.
            }
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, EyeProtectionService::class.java)
            .setAction(ACTION_STOP)

        val stopPendingIntent = PendingIntent.getForegroundService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_eye)
            .setContentTitle("\uD83D\uDC41 \u0645\u0648\u0642\u0639\u06CC\u062A \u0686\u0634\u0645 \u0641\u0639\u0627\u0644 \u0627\u0633\u062A")
            .setContentText("\u0632\u0645\u0627\u0646\u0628\u0646\u062F\u06CC \u0633\u062A\u0631\u0627\u062D\u062A \u0627\u0635\u0644\u06CC \u0627\u0633\u062A")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent)
            .addAction(0, "\u062A\u0648\u0642\u0641", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Eye Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleBreakAlarm(workIntervalMinutes: Int) {
        cancelAlarm()

        val triggerAtMillis = System.currentTimeMillis() +
            TimeUnit.MINUTES.toMillis(workIntervalMinutes.toLong())

        val intent = Intent(this, EyeProtectionService::class.java)
            .setAction(ACTION_SHOW_BREAK)

        val pendingIntent = PendingIntent.getForegroundService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (securityException: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (_: Exception) {
            // Fail silently for MVP.
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(this, EyeProtectionService::class.java)
            .setAction(ACTION_SHOW_BREAK)

        val pendingIntent = PendingIntent.getForegroundService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun showBreakOverlay(breakDurationSeconds: Int) {
        if (overlayView != null) {
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            stopProtection()
            return
        }

        acquireBreakWakeLock(breakDurationSeconds)

        val remainingSeconds = MutableStateFlow(breakDurationSeconds)
        val finished = MutableStateFlow(false)

        val composeView = ComposeView(this)

        composeView.setViewTreeLifecycleOwner(this)

        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this)
        )

        composeView.setContent {
            EyeGuardTheme {
                BreakOverlayContent(
                    remainingSeconds = remainingSeconds.collectAsState().value,
                    finished = finished.collectAsState().value,
                    onContinue = {
                        handleContinue()
                    }
                )
            }
        }

        val layoutParams = createOverlayLayoutParams()

        try {
            windowManager.addView(composeView, layoutParams)
            overlayView = composeView
        } catch (throwable: Throwable) {
            releaseWakeLock()
            stopProtection()
            return
        }

        breakJob?.cancel()

        breakJob = lifecycleScope.launch {
            for (second in breakDurationSeconds downTo 1) {
                remainingSeconds.value = second
                delay(1_000)
            }

            finished.value = true
            enableOverlayTouches()
        }
    }

    private fun createOverlayLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags =
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun enableOverlayTouches() {
        val view = overlayView ?: return

        val params = view.layoutParams as WindowManager.LayoutParams

        params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()

        try {
            windowManager.updateViewLayout(view, params)
        } catch (_: Exception) {
            // Ignore layout update failures.
        }
    }

    private fun handleContinue() {
        hideOverlay()

        lifecycleScope.launch {
            val settings = repository.settings.first()

            if (settings.enabled) {
                scheduleBreakAlarm(settings.workIntervalMinutes)
            } else {
                stopProtection()
            }
        }
    }

    private fun hideOverlay() {
        breakJob?.cancel()
        breakJob = null

        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
                // Ignore removal failures.
            }
        }

        overlayView = null
        releaseWakeLock()
    }

    private fun stopProtection() {
        cancelAlarm()
        hideOverlay()

        lifecycleScope.launch {
            repository.update {
                it.copy(enabled = false)
            }

            ServiceCompat.stopForeground(
                this@EyeProtectionService,
                ServiceCompat.STOP_FOREGROUND_REMOVE
            )

            stopSelf()
        }
    }

    private fun acquireBreakWakeLock(breakSeconds: Int) {
        releaseWakeLock()

        breakWakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EyeGuard:Break"
        ).apply {
            setReferenceCounted(false)
            acquire(
                TimeUnit.SECONDS.toMillis(breakSeconds.toLong()) +
                    TimeUnit.MINUTES.toMillis(5)
            )
        }
    }

    private fun releaseWakeLock() {
        try {
            breakWakeLock?.takeIf { it.isHeld }?.release()
        } catch (_: Exception) {
            // Ignore wake lock release failures.
        }

        breakWakeLock = null
    }
}
