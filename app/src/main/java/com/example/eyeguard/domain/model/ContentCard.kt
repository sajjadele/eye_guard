package com.example.eyeguard.domain.model

data class ContentCard(
    val id: Long = 0,
    val category: ContentCategory,
    val title: String,
    val content: String,
    val translation: String? = null,
    val example: String? = null,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
