package com.example.eyeguard.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.eyeguard.presentation.theme.BluePrimary
import com.example.eyeguard.presentation.theme.GreenSecondary

@Composable
fun EyeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eye_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val irisColor = if (isActive) GreenSecondary else BluePrimary

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2
        val cy = this.size.height / 2
        val outerRadius = this.size.minDimension / 2

        // Outer glow ring
        drawCircle(
            color = irisColor.copy(alpha = glowAlpha * 0.3f),
            radius = outerRadius * pulseScale,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )

        // Eye outline (almond shape using oval)
        drawOval(
            color = irisColor.copy(alpha = 0.15f),
            size = androidx.compose.ui.geometry.Size(
                width = outerRadius * 1.8f,
                height = outerRadius * 1.1f
            ),
            topLeft = androidx.compose.ui.geometry.Offset(
                cx - outerRadius * 0.9f,
                cy - outerRadius * 0.55f
            )
        )

        // Eye border stroke
        drawOval(
            color = irisColor.copy(alpha = 0.6f),
            size = androidx.compose.ui.geometry.Size(
                width = outerRadius * 1.8f,
                height = outerRadius * 1.1f
            ),
            topLeft = androidx.compose.ui.geometry.Offset(
                cx - outerRadius * 0.9f,
                cy - outerRadius * 0.55f
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Iris
        drawCircle(
            color = irisColor.copy(alpha = 0.9f),
            radius = outerRadius * 0.35f * pulseScale,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )

        // Pupil
        drawCircle(
            color = androidx.compose.ui.graphics.Color.Black,
            radius = outerRadius * 0.15f,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )

        // Highlight
        drawCircle(
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
            radius = outerRadius * 0.06f,
            center = androidx.compose.ui.geometry.Offset(
                cx - outerRadius * 0.1f,
                cy - outerRadius * 0.1f
            )
        )
    }
}
