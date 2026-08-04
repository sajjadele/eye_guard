package com.example.eyeguard.presentation.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.os.PowerManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eyeguard.BuildConfig
import com.example.eyeguard.R
import com.example.eyeguard.domain.models.BreakEndAlertType
import com.example.eyeguard.domain.models.DailyStats
import com.example.eyeguard.domain.models.EyeGuardSettings
import com.example.eyeguard.presentation.EyeGuardViewModel
import com.example.eyeguard.presentation.components.PermissionCard
import com.example.eyeguard.service.startEyeProtection
import com.example.eyeguard.service.startEyeProtectionTest
import com.example.eyeguard.service.stopEyeProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: EyeGuardViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var customWorkMinutes by remember { mutableStateOf("") }
    var customBreakSeconds by remember { mutableStateOf("") }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = stringResource(R.string.screen_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.screen_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                DailyStatsCard(
                    stats = uiState.dailyStats
                )

                // Permission cards
                if (!overlayGranted.value) {
                    PermissionCard(
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
                    PermissionCard(
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
                    PermissionCard(
                        title = stringResource(R.string.permission_exact_alarm_title),
                        description = stringResource(R.string.permission_exact_alarm_description),
                        actionLabel = stringResource(R.string.permission_exact_alarm_action),
                        optional = true,
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
                    PermissionCard(
                        title = stringResource(R.string.permission_battery_title),
                        description = stringResource(R.string.permission_battery_description),
                        actionLabel = stringResource(R.string.permission_battery_action),
                        optional = true,
                        onAction = {
                            runCatching {
                                batterySettingsLauncher.launch(
                                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                )
                            }
                        }
                    )
                }

                // Settings card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Work interval
                        Text(
                            text = stringResource(R.string.work_interval_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EyeGuardSettings.WORK_INTERVALS.forEach { minutes ->
                                FilterChip(
                                    selected = uiState.settings.workIntervalMinutes == minutes,
                                    onClick = {
                                        viewModel.setWorkInterval(minutes)
                                        customWorkMinutes = ""
                                    },
                                    label = {
                                        Text(stringResource(R.string.chip_work_minutes, minutes))
                                    },
                                    enabled = !isRunning,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            if (BuildConfig.DEBUG_TEST_INTERVAL) {
                                FilterChip(
                                    selected = debugTestIntervalSelected,
                                    onClick = {
                                        debugTestIntervalSelected = !debugTestIntervalSelected
                                    },
                                    label = {
                                        Text(stringResource(R.string.debug_test_chip))
                                    },
                                    enabled = !isRunning,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                )
                            }
                        }

                        // Custom work interval
                        OutlinedTextField(
                            value = customWorkMinutes,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 3) {
                                    customWorkMinutes = value
                                    val minutes = value.toIntOrNull()
                                    if (minutes != null && minutes in 1..180) {
                                        viewModel.setWorkInterval(minutes)
                                    }
                                }
                            },
                            label = {
                                Text(stringResource(R.string.field_custom_label))
                            },
                            suffix = {
                                Text(stringResource(R.string.unit_minutes))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !isRunning,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Break duration
                        Text(
                            text = stringResource(R.string.break_duration_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EyeGuardSettings.BREAK_DURATIONS.forEach { seconds ->
                                FilterChip(
                                    selected = uiState.settings.breakDurationSeconds == seconds,
                                    onClick = {
                                        viewModel.setBreakDuration(seconds)
                                        customBreakSeconds = ""
                                    },
                                    label = {
                                        Text(stringResource(R.string.chip_break_seconds, seconds))
                                    },
                                    enabled = !isRunning,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        // Custom break duration
                        OutlinedTextField(
                            value = customBreakSeconds,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 3) {
                                    customBreakSeconds = value
                                    val seconds = value.toIntOrNull()
                                    if (seconds != null && seconds in 5..300) {
                                        viewModel.setBreakDuration(seconds)
                                    }
                                }
                            },
                            label = {
                                Text(stringResource(R.string.field_custom_label))
                            },
                            suffix = {
                                Text(stringResource(R.string.unit_seconds))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !isRunning,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Break end alert
                        Text(
                            text = stringResource(R.string.break_end_alert_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BreakEndAlertType.entries.forEach { type ->
                                FilterChip(
                                    selected = uiState.settings.breakEndAlertType == type,
                                    onClick = {
                                        viewModel.setBreakEndAlertType(type)
                                    },
                                    label = {
                                        Text(breakEndAlertLabel(type))
                                    },
                                    enabled = !isRunning,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // Start/Stop button
                Button(
                    onClick = {
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
                    },
                    enabled = isRunning || requiredPermissionsGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isRunning) {
                            stringResource(R.string.stop_button)
                        } else {
                            stringResource(R.string.start_button)
                        },
                        fontSize = 16.sp
                    )
                }

                if (!requiredPermissionsGranted && !isRunning) {
                    Text(
                        text = stringResource(R.string.permission_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyStatsCard(
    stats: DailyStats,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.stats_breaks_done, stats.breaksCompleted),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(
                    R.string.stats_active_protection,
                    formatDuration(stats.activeSeconds)
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(
                    R.string.stats_break_time,
                    formatDuration(stats.breakSeconds)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun formatDuration(seconds: Long): String {
    if (seconds < 60) {
        return stringResource(R.string.format_seconds, seconds)
    }

    val totalMinutes = seconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours >= 1 && minutes > 0 ->
            stringResource(R.string.format_hours_minutes, hours, minutes)
        hours >= 1 ->
            stringResource(R.string.format_hours, hours)
        else ->
            stringResource(R.string.format_minutes, totalMinutes)
    }
}

@Composable
private fun breakEndAlertLabel(type: BreakEndAlertType): String {
    return when (type) {
        BreakEndAlertType.SOUND -> stringResource(R.string.alert_sound)
        BreakEndAlertType.VIBRATION -> stringResource(R.string.alert_vibration)
        BreakEndAlertType.BOTH -> stringResource(R.string.alert_sound_vibration)
        BreakEndAlertType.NONE -> stringResource(R.string.alert_off)
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
