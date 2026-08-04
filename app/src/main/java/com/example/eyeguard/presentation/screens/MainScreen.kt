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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var customWorkMinutes by remember { mutableStateOf("") }
    var customBreakSeconds by remember { mutableStateOf("") }

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
                // Title
                Text(
                    text = "\uD83D\uDC41 \u0645\u0648\u0642\u0639\u06CC\u062A \u0686\u0634\u0645",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "\u062E\u0627\u0633\u062A\u06AF\u0627\u0631\u06CC \u0631\u0627 \u0628\u0631\u0627\u06CC \u0637\u0648\u0644 \u0628\u0644\u0646\u062F\u06CC \u0686\u0634\u0645 \u062A\u0648\u0636\u062D \u0645\u06CC \u06A9\u0646\u062F.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                // Permission cards
                if (!overlayGranted.value) {
                    PermissionCard(
                        title = "\u0646\u0645\u0627\u06CC\u0634 \u0631\u0648\u06CC \u0627\u067E\u0644\u06CC\u06A9\u06CC\u0634\u0646\u0646\u06AF\u0627\u0646",
                        description = "\u0628\u0631\u0627\u06CC \u0646\u0645\u0627\u06CC\u0634 \u0635\u0641\u062D\u0647 \u0633\u0646\u062C\u06CC \u0628\u0627\u0631 \u0627\u0633\u062A\u0641\u0627\u062F\u0647 \u0645\u06CC \u0634\u0648\u062F.",
                        actionLabel = "\u0628\u0627\u0632 \u0628\u0631\u0646\u0627\u0645\u0647 \u0631\u0627 \u0628\u0627\u0632 \u06A9\u0646\u06CC\u062F",
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
                        title = "\u0646\u0648\u062A\u06CC\u0641\u06CC\u06A9\u06CC\u0634\u0646\u0646\u0647\u0627",
                        description = "\u0627\u0646\u062F\u0631\u0648\u06CC\u062F \u0628\u0631\u0627\u06CC \u0633\u0631\u0648\u06CC\u0633 \u067E\u0634\u062A\u0628\u0627\u0646\u06CC \u0628\u0647 \u0646\u0648\u062A\u06CC\u0641\u06CC\u06A9\u0633\u06CC\u0648\u0646 \u0646\u06CC\u0627\u0632 \u062F\u0627\u0631\u062F.",
                        actionLabel = "\u0627\u0639\u062A\u0645\u0627\u062F \u062F\u0633\u062A\u0631\u0633\u06CC \u0628\u0647 \u0646\u0648\u062A\u06CC\u0641\u06CC\u06A9\u06CC\u0634\u0646\u0646\u0647\u0627",
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
                        title = "\u0632\u0645\u0627\u0646\u0628\u0646\u062F\u06CC \u062F\u0642\u06CC\u0642",
                        description = "\u062F\u0642\u062A \u0631\u0633\u0627\u0646\u06CC \u062A\u063A\u06CC\u06CC\u0631 \u0645\u06CC \u06A9\u0646\u062F. \u0628\u062F\u0648\u0646 \u0622\u0646 \u0627\u0632 \u0622\u0646 \u0627\u0646\u062F\u0631\u0648\u06CC\u062F \u0627\u0646\u062F\u0631\u0648\u06CC\u062F \u0645\u0645\u06A9\u0646 \u0627\u0633\u062A \u0628\u0647 \u062A\u0623\u0648\u0646\u0631\u06CC \u0646\u0634\u0627\u0646\u062F.",
                        actionLabel = "\u0627\u062C\u0627\u0632\u0647 \u062F\u0627\u0646\u0631\u0648\u062C\u0648\u062F\u0646 \u0632\u0645\u0627\u0646\u0628\u0646\u062F\u06CC \u062F\u0642\u06CC\u0642",
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
                        title = "\u0628\u0647\u06CC\u0646\u0647\u0631\u06CC \u0628\u0627\u062A\u0631\u06CC",
                        description = "\u0628\u0631\u062E\u06CC \u062F\u0633\u062A\u06AF\u0627\u0647\u200C\u0647\u0627 \u0628\u0647 \u0635\u0648\u0631\u062A \u0628\u0646\u062F \u0628\u0627\u0631 \u0627\u067E\u0644\u06CC\u06A9\u06CC\u0634\u0646\u0646\u06AF\u0627\u0646 \u062E\u0648\u062F\u0631 \u0645\u06CC \u06A9\u0646\u0646\u062F.",
                        actionLabel = "\u0628\u0627\u0632 \u0635\u0641\u062D\u0647 \u0628\u0647\u06CC\u0646\u0647\u0631\u06CC",
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
                            text = "\u0641\u0627\u0635\u0644\u0647 \u06A9\u0627\u0631\u06CC",
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
                                        Text("$minutes \u062F\u0642\u06CC\u0642\u0647")
                                    },
                                    enabled = !isRunning,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                                Text("\u0635\u0641\u062D\u0647 \u062F\u0644\u062E\u0648\u0627\u0647")
                            },
                            suffix = {
                                Text("\u062F\u0642\u06CC\u0642\u0647")
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
                            text = "\u0645\u062F\u062A \u0633\u062A\u0631\u0627\u062D\u062A",
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
                                        Text("$seconds \u062A\u0646\u0648\u0647")
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
                                Text("\u0635\u0641\u062D\u0647 \u062F\u0644\u062E\u0648\u0627\u0647")
                            },
                            suffix = {
                                Text("\u062A\u0646\u0648\u0647")
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
                                context.startEyeProtection(uiState.settings)
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
                        text = if (isRunning) "\u062A\u0648\u0642\u0641 \u0645\u0648\u0642\u0639\u06CC\u062A" else "\u0634\u0631\u0648\u0639 \u0645\u0648\u0642\u0639\u06CC\u062A",
                        fontSize = 16.sp
                    )
                }

                if (!requiredPermissionsGranted && !isRunning) {
                    Text(
                        text = "\u0628\u0631\u0627\u06CC \u0634\u0631\u0648\u0639 \u0645\u0648\u0642\u0639\u06CC\u062A \u0627\u0632 \u0645\u062C\u0648\u0632\u0632\u06CC \u062F\u0633\u062A\u0631\u0633\u06CC\u200C\u0647\u0627\u06CC \u0627\u0635\u0644\u06CC \u0627\u0633\u062A\u0641\u0627\u062F\u0647 \u06A9\u0646\u06CC\u062F.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
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
