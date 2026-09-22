package com.example.eyeguard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.eyeguard.presentation.theme.DarkBg
import com.example.eyeguard.presentation.theme.GlassDarkBorder
import com.example.eyeguard.presentation.theme.GlassDarkHighlight
import com.example.eyeguard.presentation.theme.GlassDarkSurface
import com.example.eyeguard.presentation.theme.GlassLightBorder
import com.example.eyeguard.presentation.theme.GlassLightHighlight
import com.example.eyeguard.presentation.theme.GlassLightSurface

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    surfaceColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBg
    val shape = RoundedCornerShape(cornerRadius)

    val actualSurface = surfaceColor ?: if (isDark) GlassDarkSurface else GlassLightSurface
    val borderBrush = if (borderColor != null) {
        Brush.linearGradient(listOf(borderColor, borderColor))
    } else if (isDark) {
        Brush.linearGradient(listOf(GlassDarkHighlight, GlassDarkBorder))
    } else {
        Brush.linearGradient(listOf(GlassLightHighlight, GlassLightBorder))
    }

    val boxModifier = if (!isDark) {
        modifier.shadow(elevation = 6.dp, shape = shape, spotColor = Color(0x14000000))
    } else {
        modifier
    }

    Box(
        modifier = boxModifier
            .clip(shape)
            .background(actualSurface)
            .border(borderWidth, borderBrush, shape)
            .padding(20.dp),
        content = content
    )
}
