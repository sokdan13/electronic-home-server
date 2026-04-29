package com.example.domain.model

import kotlinx.serialization.Serializable

enum class AnnouncementCategory(val label: String) {
    IMPORTANT("Важно"),
    NEWS("Новости"),
    TIPS("Советы")
}

@Serializable
data class AnnouncementResponse(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val categoryLabel: String,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String
)