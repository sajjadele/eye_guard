package com.example.eyeguard.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {

    @Query("SELECT * FROM content_cards WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCard(category: String): ContentCardEntity?

    @Query("SELECT * FROM content_cards WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedCards(): Flow<List<ContentCardEntity>>

    @Query("SELECT * FROM content_cards WHERE isSaved = 1 ORDER BY createdAt DESC")
    suspend fun getSavedCardsList(): List<ContentCardEntity>

    @Query("UPDATE content_cards SET isSaved = :isSaved WHERE id = :cardId")
    suspend fun updateSavedStatus(cardId: Long, isSaved: Boolean)

    @Query("SELECT COUNT(*) FROM content_cards")
    suspend fun getCardCount(): Int

    @Query("SELECT COUNT(*) FROM content_cards WHERE category = 'EYE_CARE'")
    fun getEyeCareCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM content_cards WHERE category = 'EYE_CARE'")
    suspend fun getEyeCareCount(): Int

    @Query("SELECT title FROM content_cards")
    suspend fun getAllTitles(): List<String>

    @Query("SELECT content FROM content_cards")
    suspend fun getAllContents(): List<String>

    @Query("DELETE FROM content_cards WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<ContentCardEntity>)

    @Query("DELETE FROM content_cards")
    suspend fun deleteAll()
}
