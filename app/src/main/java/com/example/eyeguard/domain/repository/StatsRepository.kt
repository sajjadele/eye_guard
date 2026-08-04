package com.example.eyeguard.domain.repository

import com.example.eyeguard.domain.models.DailyStats

interface StatsRepository {

    suspend fun getDailyStats(): DailyStats

    suspend fun recordBreakStarted(durationSeconds: Int): Long

    suspend fun markBreakCompleted(eventId: Long)

    suspend fun recordProtectionStarted()

    suspend fun recordProtectionStopped()
}
