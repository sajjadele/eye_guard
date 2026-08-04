package com.example.eyeguard.domain.models

data class DailyStats(
    val breaksCompleted: Int = 0,
    val activeSeconds: Long = 0,
    val breakSeconds: Long = 0
) {
    companion object {
        val EMPTY = DailyStats()
    }
}
