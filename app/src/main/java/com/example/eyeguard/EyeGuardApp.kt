package com.example.eyeguard

import android.app.Application
import com.example.eyeguard.data.preferences.settingsDataStore
import com.example.eyeguard.data.repository.SettingsRepositoryImpl
import com.example.eyeguard.domain.repository.SettingsRepository

class EyeGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        EyeGuardContainer.init(this)
    }
}

object EyeGuardContainer {

    lateinit var settingsRepository: SettingsRepository
        private set

    fun init(app: EyeGuardApp) {
        settingsRepository = SettingsRepositoryImpl(app.settingsDataStore)
    }
}
