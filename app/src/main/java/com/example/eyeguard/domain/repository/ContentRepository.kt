package com.example.eyeguard.domain.repository

import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.domain.model.ContentCategory
import kotlinx.coroutines.flow.Flow

interface ContentRepository {

    suspend fun getRandomCard(category: ContentCategory): ContentCard?

    suspend fun getCardsForBreak(breakDurationSeconds: Int, vocabularyEnabled: Boolean): List<ContentCard>

    suspend fun updateSavedStatus(cardId: Long, isSaved: Boolean)

    fun getSavedCards(): Flow<List<ContentCard>>

    suspend fun getSavedCardsList(): List<ContentCard>

    suspend fun initializeCatalog()
}
