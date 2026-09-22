package com.example.eyeguard.presentation.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eyeguard.BuildConfig
import com.example.eyeguard.R
import com.example.eyeguard.presentation.EyeGuardViewModel
import com.example.eyeguard.presentation.components.ControlCard
import com.example.eyeguard.presentation.components.GlassCard
import com.example.eyeguard.presentation.components.HeroSection
import com.example.eyeguard.presentation.components.PermissionBanner
import com.example.eyeguard.presentation.components.SettingsSection
import com.example.eyeguard.presentation.components.StatsCard
import com.example.eyeguard.presentation.theme.GradientDarkEnd
import com.example.eyeguard.presentation.theme.GradientDarkStart
import com.example.eyeguard.presentation.theme.StatusWarning
import com.example.eyeguard.presentation.theme.TextSecondary
import com.example.eyeguard.service.startEyeProtection
import com.example.eyeguard.service.startEyeProtectionTest
import com.example.eyeguard.service.stopEyeProtection
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.eyeguard.presentation.components.ContentCardView

@Composable
fun MainScreen(
    viewModel: EyeGuardViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var debugTestIntervalSelected by remember { mutableStateOf(false) }

    val overlayGranted = remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    val notificationGranted = remember {
        mutableStateOf(notificationPermissionGranted(context))
    }

    val exactAlarmGranted = remember {
        mutableStateOf(exactAlarmPermissionGranted(context))
    }

    val batteryOptimizationIgnored = remember {
        mutableStateOf(batteryOptimizationIgnored(context))
    }

    fun refreshPermissions() {
        overlayGranted.value = Settings.canDrawOverlays(context)
        notificationGranted.value = notificationPermissionGranted(context)
        exactAlarmGranted.value = exactAlarmPermissionGranted(context)
        batteryOptimizationIgnored.value = batteryOptimizationIgnored(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
                viewModel.refreshDailyStats()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val requestNotificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    val batterySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    val isRunning = uiState.settings.enabled
    val requiredPermissionsGranted = overlayGranted.value && notificationGranted.value

    Scaffold { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                    val isDark = MaterialTheme.colorScheme.background == com.example.eyeguard.presentation.theme.DarkBg
                    val bgColors = if (isDark) {
                        listOf(
                            com.example.eyeguard.presentation.theme.WarmDarkBgStart,
                            com.example.eyeguard.presentation.theme.WarmDarkBgMid,
                            com.example.eyeguard.presentation.theme.WarmDarkBgEnd
                        )
                    } else {
                        listOf(
                            com.example.eyeguard.presentation.theme.WarmLightBgStart,
                            com.example.eyeguard.presentation.theme.WarmLightBgMid,
                            com.example.eyeguard.presentation.theme.WarmLightBgEnd
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(Brush.verticalGradient(colors = bgColors))
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        HeroSection(
                            isProtectionActive = isRunning,
                            themeMode = uiState.settings.themeMode,
                            onToggleTheme = { viewModel.toggleTheme() }
                        )

                    StatsCard(
                        stats = uiState.dailyStats
                    )

                    ControlCard(
                        isProtectionActive = isRunning,
                        workIntervalMinutes = uiState.settings.workIntervalMinutes,
                        breakDurationSeconds = uiState.settings.breakDurationSeconds,
                        enabled = isRunning || requiredPermissionsGranted,
                        onStartStop = {
                            if (isRunning) {
                                viewModel.setEnabled(false)
                                runCatching {
                                    context.stopEyeProtection()
                                }
                            } else {
                                viewModel.setEnabled(true)
                                runCatching {
                                    if (BuildConfig.DEBUG_TEST_INTERVAL && debugTestIntervalSelected) {
                                        context.startEyeProtectionTest(uiState.settings)
                                    } else {
                                        context.startEyeProtection(uiState.settings)
                                    }
                                }.onFailure {
                                    viewModel.setEnabled(false)
                                }
                            }
                        }
                    )

                    if (!requiredPermissionsGranted && !isRunning) {
                        Text(
                            text = stringResource(R.string.permission_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusWarning,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (!overlayGranted.value) {
                        PermissionBanner(
                            title = stringResource(R.string.permission_overlay_title),
                            description = stringResource(R.string.permission_overlay_description),
                            actionLabel = stringResource(R.string.permission_overlay_action),
                            onAction = {
                                runCatching {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    overlaySettingsLauncher.launch(intent)
                                }
                            }
                        )
                    }

                    if (!notificationGranted.value) {
                        PermissionBanner(
                            title = stringResource(R.string.permission_notification_title),
                            description = stringResource(R.string.permission_notification_description),
                            actionLabel = stringResource(R.string.permission_notification_action),
                            onAction = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    requestNotificationLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                } else {
                                    refreshPermissions()
                                }
                            }
                        )
                    }

                    if (!exactAlarmGranted.value) {
                        PermissionBanner(
                            title = stringResource(R.string.permission_exact_alarm_title),
                            description = stringResource(R.string.permission_exact_alarm_description),
                            actionLabel = stringResource(R.string.permission_exact_alarm_action),
                            isOptional = true,
                            onAction = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    runCatching {
                                        val intent = Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        exactAlarmLauncher.launch(intent)
                                    }
                                }
                            }
                        )
                    }

                    if (!batteryOptimizationIgnored.value) {
                        PermissionBanner(
                            title = stringResource(R.string.permission_battery_title),
                            description = stringResource(R.string.permission_battery_description),
                            actionLabel = stringResource(R.string.permission_battery_action),
                            isOptional = true,
                            onAction = {
                                runCatching {
                                    batterySettingsLauncher.launch(
                                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    )
                                }
                            }
                        )
                    }

                    SettingsSection(
                        workIntervalMinutes = uiState.settings.workIntervalMinutes,
                        breakDurationSeconds = uiState.settings.breakDurationSeconds,
                        breakEndAlertType = uiState.settings.breakEndAlertType,
                        isProtectionRunning = isRunning,
                        onWorkIntervalChange = { viewModel.setWorkInterval(it) },
                        onBreakDurationChange = { viewModel.setBreakDuration(it) },
                        onBreakEndAlertChange = { viewModel.setBreakEndAlertType(it) }
                    )

                    // Eye Care Tips & Online Sync Section
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "💡", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = stringResource(R.string.eye_care_tips_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { viewModel.syncTips() },
                                    enabled = !uiState.isSyncingTips,
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isSyncingTips) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = stringResource(R.string.eye_care_tips_sync_now),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.eye_care_tips_count, uiState.tipsCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                uiState.syncMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            uiState.randomTip?.let { tip ->
                                ContentCardView(
                                    card = tip,
                                    surfaceColor = Color.Transparent
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { viewModel.loadRandomTip() }
                                    ) {
                                        Text(
                                            text = "نکته بعدی ↻",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun notificationPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun exactAlarmPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

private fun batteryOptimizationIgnored(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}
