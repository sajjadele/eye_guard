package com.example.eyeguard.domain.repository

import com.example.eyeguard.domain.models.EyeGuardSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val settings: Flow<EyeGuardSettings>

    suspend fun update(transform: (EyeGuardSettings) -> EyeGuardSettings)
}
