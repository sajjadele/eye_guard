package com.example.eyeguard

import android.app.Application
import com.example.eyeguard.data.local.db.EyeGuardDatabase
import com.example.eyeguard.data.preferences.settingsDataStore
import com.example.eyeguard.data.repository.ContentRepositoryImpl
import com.example.eyeguard.data.repository.SettingsRepositoryImpl
import com.example.eyeguard.data.repository.StatsRepositoryImpl
import com.example.eyeguard.domain.repository.ContentRepository
import com.example.eyeguard.domain.repository.SettingsRepository
import com.example.eyeguard.domain.repository.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EyeGuardApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        EyeGuardContainer.init(this)
        applicationScope.launch {
            EyeGuardContainer.contentRepository.initializeCatalog()
        }
    }
}

object EyeGuardContainer {

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var statsRepository: StatsRepository
        private set

    lateinit var contentRepository: ContentRepository
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
        contentRepository = ContentRepositoryImpl(
            database.contentDao(),
            app
        )
    }
}
