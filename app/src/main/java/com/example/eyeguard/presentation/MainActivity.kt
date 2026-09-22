package com.example.eyeguard.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eyeguard.EyeGuardContainer
import com.example.eyeguard.domain.models.AppThemeMode
import com.example.eyeguard.presentation.screens.MainScreen
import com.example.eyeguard.presentation.theme.EyeGuardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: EyeGuardViewModel = viewModel(
                factory = EyeGuardViewModelFactory(
                    EyeGuardContainer.settingsRepository,
                    EyeGuardContainer.statsRepository,
                    EyeGuardContainer.contentRepository
                )
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = when (uiState.settings.themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            EyeGuardTheme(darkTheme = isDark) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
