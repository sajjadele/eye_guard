package com.example.eyeguard.domain.usecases

import com.example.eyeguard.domain.models.EyeGuardSettings
import com.example.eyeguard.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<EyeGuardSettings> {
        return repository.settings
    }
}

class UpdateSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(
        transform: (EyeGuardSettings) -> EyeGuardSettings
    ) {
        repository.update(transform)
    }
}
