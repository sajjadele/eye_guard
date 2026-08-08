package com.example.eyeguard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyeguard.R
import com.example.eyeguard.presentation.theme.BluePrimary
import com.example.eyeguard.presentation.theme.GreenSecondary
import com.example.eyeguard.presentation.theme.TextSecondary
import com.example.eyeguard.domain.models.DailyStats

@Composable
fun StatsCard(
    stats: DailyStats,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = formatDuration(stats.activeSeconds),
                    label = stringResource(R.string.stat_protection_time),
                    color = BluePrimary,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    value = stats.breaksCompleted.toString(),
                    label = stringResource(R.string.stat_breaks_done),
                    color = GreenSecondary,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    value = formatDuration(stats.breakSeconds),
                    label = stringResource(R.string.stat_break_time),
                    color = BluePrimary,
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
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 22.sp
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1
        )
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
