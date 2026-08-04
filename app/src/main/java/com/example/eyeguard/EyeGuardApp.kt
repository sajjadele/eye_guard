package com.example.eyeguard

import android.app.Application
import com.example.eyeguard.data.local.db.EyeGuardDatabase
import com.example.eyeguard.data.preferences.settingsDataStore
import com.example.eyeguard.data.repository.SettingsRepositoryImpl
import com.example.eyeguard.data.repository.StatsRepositoryImpl
import com.example.eyeguard.domain.repository.SettingsRepository
import com.example.eyeguard.domain.repository.StatsRepository

class EyeGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        EyeGuardContainer.init(this)
    }
}

object EyeGuardContainer {

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var statsRepository: StatsRepository
        private set

    lateinit var database: EyeGuardDatabase
        private set

    fun init(app: EyeGuardApp) {
        database = EyeGuardDatabase.getInstance(app)
        settingsRepository = SettingsRepositoryImpl(app.settingsDataStore)
        statsRepository = StatsRepositoryImpl(
            database.breakEventDao(),
            database.protectionSessionDao()
        )
    }
}
