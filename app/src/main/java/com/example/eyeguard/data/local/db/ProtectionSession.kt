package com.example.eyeguard.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protection_sessions")
data class ProtectionSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimestamp: Long,
    val endTimestamp: Long? = null
)
