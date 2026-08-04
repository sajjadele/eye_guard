package com.example.eyeguard.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "break_events")
data class BreakEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val duration: Int,
    val completed: Boolean
)
