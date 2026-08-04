package com.example.eyeguard.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    secondary = Color(0xFF81C995),
    background = Color.Black,
    surface = Color(0xFF1B1B1F)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    secondary = Color(0xFF188038)
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
