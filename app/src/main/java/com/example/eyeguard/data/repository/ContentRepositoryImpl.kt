package com.example.eyeguard.data.repository

import android.content.Context
import android.util.Log
import com.example.eyeguard.data.local.db.ContentCardEntity
import com.example.eyeguard.data.local.db.ContentDao
import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.domain.model.ContentCategory
import com.example.eyeguard.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ContentRepositoryImpl(
    private val contentDao: ContentDao,
    private val context: Context
) : ContentRepository {

    companion object {
        private const val TAG = "ContentRepository"
        private const val REMOTE_TIPS_URL =
            "https://raw.githubusercontent.com/sajjadele/eye_guard/main/app/src/main/assets/content_seed.json"
        private const val CONNECT_TIMEOUT_MS = 6000
        private const val READ_TIMEOUT_MS = 8000
    }

    override suspend fun getRandomCard(category: ContentCategory): ContentCard? {
        val entity = contentDao.getRandomCard(category.name)
        return entity?.toDomain()
    }

    override suspend fun getCardsForBreak(breakDurationSeconds: Int): List<ContentCard> {
        val eyeCareCard = contentDao.getRandomCard(ContentCategory.EYE_CARE.name)
        return listOfNotNull(eyeCareCard?.toDomain())
    }

    override fun getTipsCount(): Flow<Int> {
        return contentDao.getEyeCareCountFlow()
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

    override suspend fun initializeCatalog() = withContext(Dispatchers.IO) {
        try {
            // Purge any legacy experimental vocabulary cards from database
            contentDao.deleteByCategory("ENGLISH_VOCABULARY")

            val existingTitles = contentDao.getAllTitles().toSet()
            val seedCards = loadSeedContent()

            val missingCards = seedCards.filter { it.title !in existingTitles }
            if (missingCards.isNotEmpty()) {
                contentDao.insertAll(missingCards)
                Log.d(TAG, "Seeded ${missingCards.size} eye care cards into database")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing content catalog", e)
        }
    }

    override suspend fun syncRemoteTips(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking for remote tips update from $REMOTE_TIPS_URL")
            val url = URL(REMOTE_TIPS_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("User-Agent", "EyeGuard-Android")
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Remote tips sync failed with HTTP $responseCode")
                return@withContext Result.failure(Exception("HTTP $responseCode"))
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val jsonObject = JSONObject(jsonString)
            val cardsArray = jsonObject.optJSONArray("cards") ?: return@withContext Result.success(0)

            val existingContents = contentDao.getAllContents().map { it.trim() }.toSet()
            val existingTitles = contentDao.getAllTitles().map { it.trim() }.toSet()

            val newEntities = mutableListOf<ContentCardEntity>()
            for (i in 0 until cardsArray.length()) {
                val cardJson = cardsArray.getJSONObject(i)
                val category = cardJson.optString("category", "EYE_CARE")
                if (category != ContentCategory.EYE_CARE.name) continue

                val title = cardJson.optString("title").trim()
                val content = cardJson.optString("content").trim()

                // Deduplicate by content or title
                if (content !in existingContents && title !in existingTitles) {
                    newEntities.add(
                        ContentCardEntity(
                            category = ContentCategory.EYE_CARE.name,
                            title = title,
                            content = content,
                            translation = cardJson.optString("translation", null),
                            example = cardJson.optString("example", null),
                            isSaved = false,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            if (newEntities.isNotEmpty()) {
                contentDao.insertAll(newEntities)
                Log.d(TAG, "Successfully synced and inserted ${newEntities.size} new eye care tips")
            } else {
                Log.d(TAG, "Tips are already up-to-date")
            }

            Result.success(newEntities.size)
        } catch (e: Exception) {
            Log.w(TAG, "Remote sync skipped or failed (offline / network error): ${e.message}")
            Result.failure(e)
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
                val category = cardJson.getString("category")
                if (category == ContentCategory.EYE_CARE.name) {
                    cards.add(
                        ContentCardEntity(
                            category = category,
                            title = cardJson.getString("title"),
                            content = cardJson.getString("content"),
                            translation = cardJson.optString("translation", null),
                            example = cardJson.optString("example", null),
                            isSaved = false,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            cards
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load seed content", e)
            emptyList()
        }
    }

    private fun ContentCardEntity.toDomain(): ContentCard {
        return ContentCard(
            id = id,
            category = runCatching { ContentCategory.valueOf(category) }.getOrDefault(ContentCategory.EYE_CARE),
            title = title,
            content = content,
            translation = translation,
            example = example,
            isSaved = isSaved,
            createdAt = createdAt
        )
    }
}
