package com.example.eyeguard.data.repository

import com.example.eyeguard.data.local.db.BreakEvent
import com.example.eyeguard.data.local.db.BreakEventDao
import com.example.eyeguard.data.local.db.ProtectionSession
import com.example.eyeguard.data.local.db.ProtectionSessionDao
import com.example.eyeguard.domain.models.DailyStats
import com.example.eyeguard.domain.repository.StatsRepository
import java.util.Calendar

class StatsRepositoryImpl(
    private val breakEventDao: BreakEventDao,
    private val protectionSessionDao: ProtectionSessionDao
) : StatsRepository {

    override suspend fun getDailyStats(): DailyStats {
        val startOfDay = startOfToday()
        val now = System.currentTimeMillis()

        val breaksCompleted = breakEventDao.countCompletedSince(startOfDay)
        val breakSeconds = breakEventDao.sumDurationSince(startOfDay)

        val activeSeconds = protectionSessionDao.getAll().sumOf { session ->
            val start = maxOf(session.startTimestamp, startOfDay)
            val end = minOf(session.endTimestamp ?: now, now)
            ((end - start) / 1000L).coerceAtLeast(0L)
        }

        return DailyStats(
            breaksCompleted = breaksCompleted,
            activeSeconds = activeSeconds,
            breakSeconds = breakSeconds
        )
    }

    override suspend fun recordBreakStarted(durationSeconds: Int): Long {
        return breakEventDao.insert(
            BreakEvent(
                timestamp = System.currentTimeMillis(),
                duration = durationSeconds,
                completed = false
            )
        )
    }

    override suspend fun markBreakCompleted(eventId: Long) {
        breakEventDao.markCompleted(eventId)
    }

    override suspend fun recordProtectionStarted() {
        protectionSessionDao.closeOngoing(System.currentTimeMillis())
        protectionSessionDao.insert(
            ProtectionSession(startTimestamp = System.currentTimeMillis())
        )
    }

    override suspend fun recordProtectionStopped() {
        protectionSessionDao.closeOngoing(System.currentTimeMillis())
    }

    private fun startOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
