package com.example.eyeguard.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eyeguard.EyeGuardContainer
import com.example.eyeguard.presentation.screens.MainScreen
import com.example.eyeguard.presentation.theme.EyeGuardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EyeGuardTheme {
                MainScreen(
                    viewModel = viewModel(
                        factory = EyeGuardViewModelFactory(
                            EyeGuardContainer.settingsRepository
                        )
                    )
                )
            }
        }
    }
}
