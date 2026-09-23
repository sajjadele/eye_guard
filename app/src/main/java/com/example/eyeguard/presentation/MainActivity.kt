package com.example.eyeguard.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("eyeguard_locale_pref", Context.MODE_PRIVATE)
        val lang = prefs.getString("language_code", "fa") ?: "fa"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

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
            val layoutDirection = if (languageCode == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            // Keep Activity resources synchronized with user's selected language
            val activity = this@MainActivity
            remember(languageCode) {
                val locale = Locale(languageCode)
                Locale.setDefault(locale)
                val config = Configuration(activity.resources.configuration).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                @Suppress("DEPRECATION")
                activity.resources.updateConfiguration(config, activity.resources.displayMetrics)

                activity.getSharedPreferences("eyeguard_locale_pref", Context.MODE_PRIVATE)
                    .edit()
                    .putString("language_code", languageCode)
                    .apply()
                true
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection
            ) {
                EyeGuardTheme(darkTheme = isDark, layoutDirection = layoutDirection) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
