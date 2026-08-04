package com.example.eyeguard.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BreakEventDao {

    @Insert
    suspend fun insert(event: BreakEvent): Long

    @Query("UPDATE break_events SET completed = 1 WHERE id = :eventId")
    suspend fun markCompleted(eventId: Long)

    @Query("SELECT COUNT(*) FROM break_events WHERE completed = 1 AND timestamp >= :startOfDay")
    suspend fun countCompletedSince(startOfDay: Long): Int

    @Query("SELECT COALESCE(SUM(duration), 0) FROM break_events WHERE completed = 1 AND timestamp >= :startOfDay")
    suspend fun sumDurationSince(startOfDay: Long): Long
}
