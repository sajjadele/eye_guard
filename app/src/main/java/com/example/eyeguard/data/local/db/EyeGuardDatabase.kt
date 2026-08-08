package com.example.eyeguard.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BreakEvent::class, ProtectionSession::class, ContentCardEntity::class],
    version = 2,
    exportSchema = false
)
abstract class EyeGuardDatabase : RoomDatabase() {

    abstract fun breakEventDao(): BreakEventDao

    abstract fun protectionSessionDao(): ProtectionSessionDao

    abstract fun contentDao(): ContentDao

    companion object {
        @Volatile
        private var instance: EyeGuardDatabase? = null

        fun getInstance(context: Context): EyeGuardDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EyeGuardDatabase::class.java,
                    "eye_guard.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
