package com.example.eyeguard.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.eyeguard.BuildConfig
import com.example.eyeguard.R
import com.example.eyeguard.domain.models.BreakEndAlertType
import com.example.eyeguard.domain.models.EyeGuardSettings
import com.example.eyeguard.presentation.theme.BluePrimary
import com.example.eyeguard.presentation.theme.BluePrimaryContainer
import com.example.eyeguard.presentation.theme.BlueOnPrimaryContainer
import com.example.eyeguard.presentation.theme.GlassBorder
import com.example.eyeguard.presentation.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSection(
    workIntervalMinutes: Int,
    breakDurationSeconds: Int,
    breakEndAlertType: BreakEndAlertType,
    isProtectionRunning: Boolean,
    onWorkIntervalChange: (Int) -> Unit,
    onBreakDurationChange: (Int) -> Unit,
    onBreakEndAlertChange: (BreakEndAlertType) -> Unit,
    modifier: Modifier = Modifier
) {
    var customWorkMinutes by remember { mutableStateOf("") }
    var customBreakSeconds by remember { mutableStateOf("") }
    var debugTestSelected by remember { mutableStateOf(false) }
    var showCustomWork by remember { mutableStateOf(false) }
    var showCustomBreak by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Work Interval ──────────────────────────
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.work_interval_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EyeGuardSettings.WORK_INTERVALS.forEach { minutes ->
                        FilterChip(
                            selected = workIntervalMinutes == minutes && !showCustomWork,
                            onClick = {
                                onWorkIntervalChange(minutes)
                                customWorkMinutes = ""
                                showCustomWork = false
                            },
                            label = { Text(stringResource(R.string.chip_work_minutes, minutes)) },
                            enabled = !isProtectionRunning,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimaryContainer,
                                selectedLabelColor = BlueOnPrimaryContainer
                            )
                        )
                    }

                    if (BuildConfig.DEBUG_TEST_INTERVAL) {
                        FilterChip(
                            selected = debugTestSelected,
                            onClick = { debugTestSelected = !debugTestSelected },
                            label = { Text(stringResource(R.string.debug_test_chip)) },
                            enabled = !isProtectionRunning,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }

                    FilterChip(
                        selected = showCustomWork,
                        onClick = { showCustomWork = !showCustomWork },
                        label = { Text(stringResource(R.string.field_custom_label)) },
                        enabled = !isProtectionRunning,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimaryContainer,
                            selectedLabelColor = BlueOnPrimaryContainer
                        )
                    )
                }

                AnimatedVisibility(
                    visible = showCustomWork,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = customWorkMinutes,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() } && value.length <= 3) {
                                customWorkMinutes = value
                                val minutes = value.toIntOrNull()
                                if (minutes != null && minutes in 1..180) {
                                    onWorkIntervalChange(minutes)
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.field_custom_label)) },
                        suffix = { Text(stringResource(R.string.unit_minutes)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isProtectionRunning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = GlassBorder
                        )
                    )
                }
            }
        }

        // ── Break Duration ─────────────────────────
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.break_duration_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EyeGuardSettings.BREAK_DURATIONS.forEach { seconds ->
                        FilterChip(
                            selected = breakDurationSeconds == seconds && !showCustomBreak,
                            onClick = {
                                onBreakDurationChange(seconds)
                                customBreakSeconds = ""
                                showCustomBreak = false
                            },
                            label = { Text(stringResource(R.string.chip_break_seconds, seconds)) },
                            enabled = !isProtectionRunning,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimaryContainer,
                                selectedLabelColor = BlueOnPrimaryContainer
                            )
                        )
                    }

                    FilterChip(
                        selected = showCustomBreak,
                        onClick = { showCustomBreak = !showCustomBreak },
                        label = { Text(stringResource(R.string.field_custom_label)) },
                        enabled = !isProtectionRunning,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimaryContainer,
                            selectedLabelColor = BlueOnPrimaryContainer
                        )
                    )
                }

                AnimatedVisibility(
                    visible = showCustomBreak,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = customBreakSeconds,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() } && value.length <= 3) {
                                customBreakSeconds = value
                                val seconds = value.toIntOrNull()
                                if (seconds != null && seconds in 5..300) {
                                    onBreakDurationChange(seconds)
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.field_custom_label)) },
                        suffix = { Text(stringResource(R.string.unit_seconds)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isProtectionRunning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = GlassBorder
                        )
                    )
                }
            }
        }

        // ── Break End Alert ────────────────────────
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.break_end_alert_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BreakEndAlertType.entries.forEach { type ->
                        FilterChip(
                            selected = breakEndAlertType == type,
                            onClick = { onBreakEndAlertChange(type) },
                            label = { Text(breakEndAlertLabel(type)) },
                            enabled = !isProtectionRunning,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimaryContainer,
                                selectedLabelColor = BlueOnPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
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
