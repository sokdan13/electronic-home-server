package com.example.data.repository

import com.example.data.table.ApartmentsTable
import com.example.data.table.GuestPassTable
import com.example.domain.model.GuestPassCreateDto
import com.example.domain.model.GuestPassResponse
import com.example.domain.model.GuestPassValidateResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

class GuestPassRepository {

    fun create(userId: String, dto: GuestPassCreateDto): GuestPassResponse = transaction {
        if (dto.durationMinutes !in listOf(5, 30, 60)) {
            throw IllegalArgumentException("Недопустимый срок действия")
        }

        val token     = generateToken()
        val now       = Instant.now()
        val expiresAt = now.plus(dto.durationMinutes.toLong(), ChronoUnit.MINUTES)

        GuestPassTable.insert {
            it[this.userId]      = userId
            it[apartmentId]      = UUID.fromString(dto.apartmentId)
            it[this.token]       = token
            it[this.expiresAt]   = expiresAt
            it[createdAt]        = now
        }

        findByToken(token)!!
    }

    fun findByUser(userId: String): List<GuestPassResponse> = transaction {
        GuestPassTable
            .selectAll().where { GuestPassTable.userId eq userId }
            .orderBy(GuestPassTable.createdAt, SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun findByToken(token: String): GuestPassResponse? = transaction {
        GuestPassTable
            .selectAll().where { GuestPassTable.token eq token }
            .singleOrNull()
            ?.toResponse()
    }

    fun validate(token: String): GuestPassValidateResponse = transaction {
        val row = GuestPassTable
            .join(ApartmentsTable, JoinType.LEFT, GuestPassTable.apartmentId, ApartmentsTable.id)
            .selectAll().where { GuestPassTable.token eq token }
            .singleOrNull()

        if (row == null) return@transaction GuestPassValidateResponse(
            isValid     = false,
            apartment   = null,
            expiresAt   = null,
            minutesLeft = 0
        )

        val expiresAt   = row[GuestPassTable.expiresAt]
        val now         = Instant.now()
        val isValid     = now.isBefore(expiresAt)
        val minutesLeft = ChronoUnit.MINUTES.between(now, expiresAt).coerceAtLeast(0)
        val aptNum      = row[ApartmentsTable.apartment]
        val street      = row[ApartmentsTable.street]
        val house       = row[ApartmentsTable.house]

        GuestPassValidateResponse(
            isValid     = isValid,
            apartment   = "кв. $aptNum, $street, д. $house",
            expiresAt   = expiresAt.toString(),
            minutesLeft = minutesLeft
        )
    }

    private fun generateToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun ResultRow.toResponse(): GuestPassResponse {
        val expiresAt   = this[GuestPassTable.expiresAt]
        val now         = Instant.now()
        val isValid     = now.isBefore(expiresAt)
        val minutesLeft = ChronoUnit.MINUTES.between(now, expiresAt).coerceAtLeast(0)
        return GuestPassResponse(
            id          = this[GuestPassTable.id].toString(),
            apartmentId = this[GuestPassTable.apartmentId].toString(),
            token       = this[GuestPassTable.token],
            expiresAt   = expiresAt.toString(),
            createdAt   = this[GuestPassTable.createdAt].toString(),
            isValid     = isValid,
            minutesLeft = minutesLeft
        )
    }
}