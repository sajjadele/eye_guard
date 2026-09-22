package com.example.eyeguard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eyeguard.R

@Composable
fun ControlCard(
    isProtectionActive: Boolean,
    workIntervalMinutes: Int,
    breakDurationSeconds: Int,
    enabled: Boolean,
    onStartStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (isProtectionActive) {
        listOf(Color(0xFFF87171), Color(0xFFDC2626))
    } else {
        listOf(Color(0xFF34D399), Color(0xFF059669))
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.control_config_summary,
                    workIntervalMinutes,
                    breakDurationSeconds
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val buttonShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = if (enabled) 10.dp else 0.dp,
                        shape = buttonShape,
                        spotColor = if (isProtectionActive) Color(0x66DC2626) else Color(0x66059669)
                    )
                    .clip(buttonShape)
                    .background(
                        if (enabled) Brush.horizontalGradient(gradientColors)
                        else Brush.horizontalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                    )
                    .clickable(enabled = enabled, onClick = onStartStop),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isProtectionActive)
                        stringResource(R.string.stop_button)
                    else
                        stringResource(R.string.start_button),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
