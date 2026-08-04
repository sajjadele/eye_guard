package com.example.eyeguard.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProtectionSessionDao {

    @Insert
    suspend fun insert(session: ProtectionSession): Long

    @Query("UPDATE protection_sessions SET endTimestamp = :endTime WHERE endTimestamp IS NULL")
    suspend fun closeOngoing(endTime: Long)

    @Query("SELECT * FROM protection_sessions")
    suspend fun getAll(): List<ProtectionSession>
}
