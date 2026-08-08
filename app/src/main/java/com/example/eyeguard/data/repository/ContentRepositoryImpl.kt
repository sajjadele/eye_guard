package com.example.eyeguard.data.repository

import android.content.Context
import com.example.eyeguard.data.local.db.ContentCardEntity
import com.example.eyeguard.data.local.db.ContentDao
import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.domain.model.ContentCategory
import com.example.eyeguard.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ContentRepositoryImpl(
    private val contentDao: ContentDao,
    private val context: Context
) : ContentRepository {

    override suspend fun getRandomCard(category: ContentCategory): ContentCard? {
        val entity = contentDao.getRandomCard(category.name)
        return entity?.toDomain()
    }

    override suspend fun getCardsForBreak(breakDurationSeconds: Int, vocabularyEnabled: Boolean): List<ContentCard> {
        val cards = mutableListOf<ContentCard>()

        val eyeCareCard = contentDao.getRandomCard(ContentCategory.EYE_CARE.name)
        eyeCareCard?.let { cards.add(it.toDomain()) }

        if (vocabularyEnabled && breakDurationSeconds > 15) {
            val vocabCard = contentDao.getRandomCard(ContentCategory.ENGLISH_VOCABULARY.name)
            vocabCard?.let { cards.add(it.toDomain()) }
        }

        val maxCards = when {
            breakDurationSeconds <= 15 -> 1
            breakDurationSeconds <= 60 -> 2
            else -> 3
        }

        return cards.take(maxCards)
    }

    override suspend fun updateSavedStatus(cardId: Long, isSaved: Boolean) {
        contentDao.updateSavedStatus(cardId, isSaved)
    }

    override fun getSavedCards(): Flow<List<ContentCard>> {
        return contentDao.getSavedCards().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSavedCardsList(): List<ContentCard> {
        return contentDao.getSavedCardsList().map { it.toDomain() }
    }

    override suspend fun initializeCatalog() {
        if (contentDao.getCardCount() == 0) {
            val cards = loadSeedContent()
            contentDao.insertAll(cards)
        }
    }

    private fun loadSeedContent(): List<ContentCardEntity> {
        return try {
            val jsonString = context.assets.open("content_seed.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val cardsArray = jsonObject.getJSONArray("cards")

            val cards = mutableListOf<ContentCardEntity>()
            for (i in 0 until cardsArray.length()) {
                val cardJson = cardsArray.getJSONObject(i)
                cards.add(
                    ContentCardEntity(
                        category = cardJson.getString("category"),
                        title = cardJson.getString("title"),
                        content = cardJson.getString("content"),
                        translation = cardJson.optString("translation", null),
                        example = cardJson.optString("example", null),
                        isSaved = false,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
            cards
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun ContentCardEntity.toDomain(): ContentCard {
        return ContentCard(
            id = id,
            category = ContentCategory.valueOf(category),
            title = title,
            content = content,
            translation = translation,
            example = example,
            isSaved = isSaved,
            createdAt = createdAt
        )
    }
}
