package com.example.eyeguard.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Vision Planner style dark theme — neumorphic, calm
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF003060),
    primaryContainer = Color(0xFF1A3A5C),
    onPrimaryContainer = Color(0xFFD3E3FD),

    secondary = Color(0xFF81C995),
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF1B3A2A),
    onSecondaryContainer = Color(0xFFC4EED4),

    tertiary = Color(0xFFD7BCFF),
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF2D2250),
    onTertiaryContainer = Color(0xFFE9DDFF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0F1114),
    onBackground = Color(0xFFE2E2E6),

    surface = Color(0xFF1A1C20),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF252830),
    onSurfaceVariant = Color(0xFFC3C6CF),

    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF44474F),

    surfaceTint = Color(0xFF8AB4F8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF003060),

    secondary = Color(0xFF188038),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC4EED4),
    onSecondaryContainer = Color(0xFF00391A),

    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),

    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C6CF)
)

@Composable
fun EyeGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
