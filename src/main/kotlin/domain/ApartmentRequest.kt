package com.example.domain

import kotlinx.serialization.Serializable

enum class ApartmentStatus { PENDING, APPROVED, REJECTED }

@Serializable
data class ApartmentRequest(
    val city: String,
    val street: String,
    val house: String,
    val building: String? = null,
    val floor: Int,
    val apartment: String
)

@Serializable
data class ApartmentResponse(
    val id: String,
    val userId: String,
    val city: String,
    val street: String,
    val house: String,
    val building: String?,
    val floor: Int,
    val apartment: String,
    val status: String,
    val accountNumber: String?,
    val rejectionNote: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ApproveRequest(
    val accountNumber: String? = null,
    val city: String? = null,
    val street: String? = null,
    val house: String? = null,
    val building: String? = null,
    val floor: Int? = null,
    val apartment: String? = null
)

@Serializable
data class RejectRequest(
    val note: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)