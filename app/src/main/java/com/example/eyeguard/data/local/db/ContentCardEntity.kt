package com.example.eyeguard.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_cards")
data class ContentCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val title: String,
    val content: String,
    val translation: String? = null,
    val example: String? = null,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
