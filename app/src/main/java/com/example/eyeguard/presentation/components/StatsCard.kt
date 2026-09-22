package com.example.eyeguard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyeguard.R
import com.example.eyeguard.domain.models.DailyStats
import com.example.eyeguard.presentation.theme.AmberWarm
import com.example.eyeguard.presentation.theme.DarkBg
import com.example.eyeguard.presentation.theme.EmeraldGlow
import com.example.eyeguard.presentation.theme.EmeraldPrimary

@Composable
fun StatsCard(
    stats: DailyStats,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBg

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatItem(
                    value = formatDuration(stats.activeSeconds),
                    label = stringResource(R.string.stat_protection_time),
                    color = if (isDark) AmberWarm else Color(0xFFD97706),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    value = stats.breaksCompleted.toString(),
                    label = stringResource(R.string.stat_breaks_done),
                    color = if (isDark) EmeraldGlow else EmeraldPrimary,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    value = formatDuration(stats.breakSeconds),
                    label = stringResource(R.string.stat_break_time),
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(16.dp)
    val pillBg = if (isDark) Color(0x1AFFFFFF) else Color(0x66F1EDE4)

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(pillBg)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontSize = 21.sp
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds < 60) {
        return "${seconds}s"
    }
    val totalMinutes = seconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours >= 1 && minutes > 0 -> "${hours}h ${minutes}m"
        hours >= 1 -> "${hours}h"
        else -> "${totalMinutes}m"
    }
}
