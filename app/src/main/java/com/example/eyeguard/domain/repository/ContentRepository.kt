package com.example.eyeguard.domain.repository

import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.domain.model.ContentCategory
import kotlinx.coroutines.flow.Flow

interface ContentRepository {

    suspend fun getRandomCard(category: ContentCategory = ContentCategory.EYE_CARE): ContentCard?

    suspend fun getCardsForBreak(breakDurationSeconds: Int): List<ContentCard>

    fun getTipsCount(): Flow<Int>

    suspend fun syncRemoteTips(): Result<Int>

    suspend fun updateSavedStatus(cardId: Long, isSaved: Boolean)

    fun getSavedCards(): Flow<List<ContentCard>>

    suspend fun getSavedCardsList(): List<ContentCard>

    suspend fun initializeCatalog()
}
