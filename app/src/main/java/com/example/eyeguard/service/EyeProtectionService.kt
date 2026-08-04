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
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.eyeguard.EyeGuardContainer
import com.example.eyeguard.R
import com.example.eyeguard.domain.models.BreakEndAlertType
import com.example.eyeguard.presentation.MainActivity
import com.example.eyeguard.presentation.theme.EyeGuardTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class EyeProtectionService : LifecycleService(), SavedStateRegistryOwner {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SHOW_BREAK = "ACTION_SHOW_BREAK"
        const val ACTION_SHOW_REMINDER_BREAK = "ACTION_SHOW_REMINDER_BREAK"
        const val ACTION_CONTINUE = "ACTION_CONTINUE"

        const val EXTRA_WORK_MINUTES = "EXTRA_WORK_MINUTES"
        const val EXTRA_BREAK_SECONDS = "EXTRA_BREAK_SECONDS"
        const val EXTRA_TEST_INTERVAL_SECONDS = "EXTRA_TEST_INTERVAL_SECONDS"

        private const val TAG = "EyeProtectionService"
        private const val NOTIFICATION_ID = 1
        private const val ERROR_NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "eye_guard_channel"
        private const val CHANNEL_INFO_ID = "eye_guard_info_channel"
        private const val REMINDER_DELAY_MINUTES = 5
        private const val REQUEST_CODE_REMINDER = 2
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

    private val savedStateRegistryController = SavedStateRegistryController.create(this@EyeProtectionService)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        super.onCreate()
        createNotificationChannel()
        savedStateRegistryController.performRestore(null)
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
                val testIntervalSeconds = intent.getIntExtra(EXTRA_TEST_INTERVAL_SECONDS, -1)

                Log.d(TAG, "Protection started (work=$workIntervalMinutes, break=$breakDurationSeconds, test=$testIntervalSeconds)")

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

                    if (testIntervalSeconds > 0) {
                        scheduleBreakAlarm(
                            workIntervalMinutes = -1,
                            testIntervalSeconds = testIntervalSeconds
                        )
                    } else {
                        scheduleBreakAlarm(settings.workIntervalMinutes)
                    }
                }
            }

            ACTION_SHOW_BREAK -> {
                Log.d(TAG, "Break alarm fired")
                lifecycleScope.launch {
                    val settings = repository.settings.first()

                    if (settings.enabled) {
                        showBreakOverlay(settings.breakDurationSeconds)
                    } else {
                        stopProtection()
                    }
                }
            }

            ACTION_SHOW_REMINDER_BREAK -> {
                Log.d(TAG, "Reminder break alarm fired")

                lifecycleScope.launch {
                    val settings = repository.settings.first()

                    if (settings.enabled) {
                        Log.d(TAG, "Showing reminder break overlay")
                        showBreakOverlay(settings.breakDurationSeconds, isReminder = true)
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
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent)
            .addAction(0, getString(R.string.notification_stop), stopPendingIntent)
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

            val infoChannel = NotificationChannel(
                CHANNEL_INFO_ID,
                "Eye Protection Info",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(infoChannel)
        }
    }

    private fun scheduleBreakAlarm(
        workIntervalMinutes: Int,
        testIntervalSeconds: Int = -1
    ) {
        cancelAlarm()

        val delayMillis: Long = if (testIntervalSeconds > 0) {
            TimeUnit.SECONDS.toMillis(testIntervalSeconds.toLong())
        } else {
            TimeUnit.MINUTES.toMillis(workIntervalMinutes.toLong())
        }

        val triggerAtMillis = System.currentTimeMillis() + delayMillis

        scheduleAlarm(ACTION_SHOW_BREAK, 0, triggerAtMillis)
    }

    private fun scheduleReminderBreak() {
        Log.d(TAG, "Reminder break scheduled in 5 minutes")

        hideOverlay()

        val triggerAtMillis = System.currentTimeMillis() +
            TimeUnit.MINUTES.toMillis(REMINDER_DELAY_MINUTES.toLong())

        scheduleAlarm(ACTION_SHOW_REMINDER_BREAK, REQUEST_CODE_REMINDER, triggerAtMillis)
    }

    private fun scheduleAlarm(action: String, requestCode: Int, triggerAtMillis: Long) {
        val intent = Intent(this, EyeProtectionService::class.java)
            .setAction(action)

        val pendingIntent = PendingIntent.getForegroundService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val delayMillis = triggerAtMillis - System.currentTimeMillis()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                Log.w(TAG, "Exact alarm permission missing; scheduling inexact alarm")
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
            Log.e(TAG, "Exact alarm denied by security policy; falling back to inexact", securityException)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (throwable: Throwable) {
            Log.e(TAG, "Failed to schedule break alarm", throwable)
            showErrorNotification(
                getString(R.string.error_overlay_failed_title),
                getString(R.string.error_overlay_failed)
            )
            return
        }

        Log.d(TAG, "Break scheduled at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(triggerAtMillis))} (in ${delayMillis / 1000}s)")
    }

    private fun cancelAlarm() {
        cancelPendingAlarm(ACTION_SHOW_BREAK, 0)
        cancelPendingAlarm(ACTION_SHOW_REMINDER_BREAK, REQUEST_CODE_REMINDER)
    }

    private fun cancelPendingAlarm(action: String, requestCode: Int) {
        val intent = Intent(this, EyeProtectionService::class.java)
            .setAction(action)

        val pendingIntent = PendingIntent.getForegroundService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun showBreakOverlay(breakDurationSeconds: Int, isReminder: Boolean = false) {
        if (overlayView != null) {
            Log.w(TAG, "Overlay already present; ignoring showBreakOverlay")
            return
        }

        val canDraw = Settings.canDrawOverlays(this)
        Log.d(TAG, "canDrawOverlays() = $canDraw")

        if (!canDraw) {
            Log.e(TAG, "Overlay failed: overlay permission not granted")
            showErrorNotification(
                getString(R.string.error_overlay_missing_permission_title),
                getString(R.string.error_overlay_missing_permission)
            )
            rescheduleAfterFailure()
            return
        }

        Log.d(TAG, "Showing break overlay ($breakDurationSeconds seconds)")

        acquireBreakWakeLock(breakDurationSeconds)

        val remainingSeconds = MutableStateFlow(breakDurationSeconds)
        val finished = MutableStateFlow(false)

        val composeView = ComposeView(this)

        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindow
        )

        composeView.setContent {
            EyeGuardTheme {
                BreakOverlayContent(
                    remainingSeconds = remainingSeconds.collectAsState().value,
                    finished = finished.collectAsState().value,
                    onAction = { action ->
                        when (action) {
                            BreakAction.Continue -> handleContinue()
                            BreakAction.RemindLater -> handleRemindLater()
                        }
                    },
                    showRemindLater = !isReminder
                )
            }
        }

        val layoutParams = createOverlayLayoutParams()

        Log.d(
            TAG,
            "addView attempt: type=${layoutParams.type}, flags=0x${layoutParams.flags.toString(16)}, " +
                "size=${layoutParams.width}x${layoutParams.height}, format=${layoutParams.format}, " +
                "gravity=${layoutParams.gravity}"
        )

        try {
            windowManager.addView(composeView, layoutParams)
            overlayView = composeView
            Log.d(
                TAG,
                "addView SUCCEEDED: attached=${composeView.isAttachedToWindow}, " +
                    "hasWindowToken=${composeView.windowToken != null}, " +
                    "size=${composeView.width}x${composeView.height}, " +
                    "visibility=${composeView.visibility}, alpha=${composeView.alpha}"
            )

            composeView.postDelayed({
                Log.d(
                    TAG,
                    "Overlay after layout pass: size=${composeView.width}x${composeView.height}, " +
                        "attached=${composeView.isAttachedToWindow}, " +
                        "hasWindowToken=${composeView.windowToken != null}, " +
                        "visibility=${composeView.visibility}, " +
                        "alpha=${composeView.alpha}"
                )
            }, 300L)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Overlay failed: windowManager.addView threw", throwable)
            releaseWakeLock()
            showErrorNotification(
                getString(R.string.error_overlay_failed_title),
                getString(
                    R.string.error_overlay_failed_detail,
                    throwable.message ?: throwable.javaClass.simpleName
                )
            )
            rescheduleAfterFailure()
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
            Log.d(TAG, "Break countdown finished; touches enabled")

            val settings = repository.settings.first()
            triggerBreakEndAlert(settings.breakEndAlertType)
        }
    }

    private suspend fun triggerBreakEndAlert(type: BreakEndAlertType) {
        when (type) {
            BreakEndAlertType.NONE -> return
            BreakEndAlertType.SOUND -> playBreakEndSound()
            BreakEndAlertType.VIBRATION -> vibrateShort()
            BreakEndAlertType.BOTH -> {
                playBreakEndSound()
                vibrateShort()
            }
        }
    }

    private suspend fun playBreakEndSound() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            Log.d(
                TAG,
                "Break end sound: ringerMode=${audioManager.ringerMode}, " +
                    "musicVolume=${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)}, " +
                    "notificationVolume=${audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)}"
            )

            val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            val started = toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
            Log.d(TAG, "Break end tone startTone result: $started")

            delay(500)
            toneGenerator.release()
            Log.d(TAG, "Break end tone played")
        } catch (throwable: Throwable) {
            Log.e(TAG, "Failed to play break-end sound", throwable)
        }
    }

    private fun vibrateShort() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
            Log.d(TAG, "Break end vibration triggered")
        } catch (throwable: Throwable) {
            Log.e(TAG, "Failed to vibrate", throwable)
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
        Log.d(TAG, "Break finished; user continued")

        lifecycleScope.launch {
            val settings = repository.settings.first()

            if (settings.enabled) {
                scheduleBreakAlarm(settings.workIntervalMinutes)
            } else {
                stopProtection()
            }
        }
    }

    private fun handleRemindLater() {
        Log.d(TAG, "User selected remind later")
        scheduleReminderBreak()
    }

    private fun rescheduleAfterFailure() {
        lifecycleScope.launch {
            val settings = repository.settings.first()

            if (settings.enabled) {
                scheduleBreakAlarm(settings.workIntervalMinutes)
            }
        }
    }

    private fun showErrorNotification(title: String, text: String) {
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_INFO_ID)
                .setSmallIcon(R.drawable.ic_eye)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Failed to show error notification", throwable)
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
        Log.d(TAG, "Stopping protection")
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
