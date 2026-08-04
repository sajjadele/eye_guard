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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eyeguard.domain.models.EyeGuardSettings
import com.example.eyeguard.presentation.EyeGuardViewModel
import com.example.eyeguard.presentation.components.PermissionCard
import com.example.eyeguard.service.startEyeProtection
import com.example.eyeguard.service.stopEyeProtection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: EyeGuardViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                Text(
                    text = "Eye Protection",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Reminds you to rest your eyes during long phone usage.",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!overlayGranted.value) {
                    PermissionCard(
                        title = "Display over other apps",
                        description = "EyeGuard needs this permission to show the full-screen break screen above the app you are currently using.",
                        actionLabel = "Open overlay settings",
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
                        title = "Notifications",
                        description = "Android requires a notification for the foreground service that keeps eye protection active.",
                        actionLabel = "Grant notification access",
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
                        title = "Exact alarms",
                        description = "Improves break timing accuracy. Without it, Android may delay the break slightly.",
                        actionLabel = "Allow exact alarms",
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
                        title = "Battery optimization",
                        description = "Some devices aggressively kill background apps. For best results, disable battery optimization for EyeGuard.",
                        actionLabel = "Open battery settings",
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

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Work interval",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EyeGuardSettings.WORK_INTERVALS.forEach { minutes ->
                                FilterChip(
                                    selected = uiState.settings.workIntervalMinutes == minutes,
                                    onClick = {
                                        viewModel.setWorkInterval(minutes)
                                    },
                                    label = {
                                        Text("$minutes min")
                                    },
                                    enabled = !isRunning
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Break duration",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EyeGuardSettings.BREAK_DURATIONS.forEach { seconds ->
                                FilterChip(
                                    selected = uiState.settings.breakDurationSeconds == seconds,
                                    onClick = {
                                        viewModel.setBreakDuration(seconds)
                                    },
                                    label = {
                                        Text("${seconds}s")
                                    },
                                    enabled = !isRunning
                                )
                            }
                        }
                    }
                }

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
                                context.startEyeProtection(uiState.settings)
                            }.onFailure {
                                viewModel.setEnabled(false)
                            }
                        }
                    },
                    enabled = isRunning || requiredPermissionsGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isRunning) "Stop Protection" else "Start Protection"
                    )
                }

                if (!requiredPermissionsGranted && !isRunning) {
                    Text(
                        text = "Grant overlay and notification permissions before starting protection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
