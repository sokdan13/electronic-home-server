package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GuestPassCreateDto(
    val apartmentId: String,
    val durationMinutes: Int
)

@Serializable
data class GuestPassResponse(
    val id: String,
    val apartmentId: String,
    val token: String,
    val expiresAt: String,
    val createdAt: String,
    val isValid: Boolean,
    val minutesLeft: Long
)

@Serializable
data class GuestPassValidateResponse(
    val isValid: Boolean,
    val apartment: String?,
    val expiresAt: String?,
    val minutesLeft: Long
)