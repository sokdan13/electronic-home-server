package com.example.domain.model

import kotlinx.serialization.Serializable

enum class RequestCategory(val label: String) {
    GARBAGE("Некачественное содержание (мусор)"),
    HOT_COLD_WATER("Неисправность ГВС/ХВС"),
    ELECTRICITY("Неисправность электричества"),
    ELEVATOR("Проблемы с лифтом"),
    CHUTE("Засор в мусоропроводе")
}

enum class RequestStatus(val label: String) {
    PENDING("В обработке"),
    IN_PROGRESS("В работе"),
    DONE("Выполнена")
}

@Serializable
data class RequestCreateDto(
    val apartmentId: String,
    val category: String,
    val description: String? = null
)

@Serializable
data class RequestResponse(
    val id: String,
    val userId: String,
    val apartmentId: String,
    val category: String,
    val categoryLabel: String,
    val description: String?,
    val status: String,
    val statusLabel: String,
    val dueDate: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class TakeInProgressDto(
    val dueDate: String
)