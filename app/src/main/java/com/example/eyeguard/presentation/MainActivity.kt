package com.example.eyeguard.presentation

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eyeguard.EyeGuardContainer
import com.example.eyeguard.domain.models.AppThemeMode
import com.example.eyeguard.presentation.screens.MainScreen
import com.example.eyeguard.presentation.theme.EyeGuardTheme
import java.util.Locale

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

            val languageCode = uiState.settings.languageCode
            val locale = remember(languageCode) { Locale(languageCode) }
            val currentContext = LocalContext.current

            val localizedContext = remember(currentContext, locale) {
                Locale.setDefault(locale)
                val config = Configuration(currentContext.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                currentContext.createConfigurationContext(config)
            }

            val layoutDirection = if (languageCode == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalLayoutDirection provides layoutDirection
            ) {
                EyeGuardTheme(darkTheme = isDark) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
