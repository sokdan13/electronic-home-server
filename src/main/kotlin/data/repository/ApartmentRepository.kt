package com.example.data.repository


import com.example.data.table.ApartmentsTable
import com.example.domain.ApartmentRequest
import com.example.domain.ApartmentResponse
import com.example.domain.ApproveRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class ApartmentRepository {

    fun create(userId: String, req: ApartmentRequest): ApartmentResponse = transaction {
        val now = Instant.now()
        val id = ApartmentsTable.insert {
            it[this.userId]    = userId
            it[city]           = req.city.trim()
            it[street]         = req.street.trim()
            it[house]          = req.house.trim()
            it[building]       = req.building?.trim()
            it[floor]          = req.floor
            it[apartment]      = req.apartment.trim()
            it[status]         = "PENDING"
            it[accountNumber]  = null
            it[rejectionNote]  = null
            it[createdAt]      = now
            it[updatedAt]      = now
        } get ApartmentsTable.id

        findById(id.toString())!!
    }

    fun findById(id: String): ApartmentResponse? = transaction {
        ApartmentsTable
            .select { ApartmentsTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toResponse()
    }

    fun findByUserId(userId: String): List<ApartmentResponse> = transaction {
        ApartmentsTable
            .select { ApartmentsTable.userId eq userId }
            .orderBy(ApartmentsTable.createdAt, SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun findAll(): List<ApartmentResponse> = transaction {
        ApartmentsTable
            .selectAll()
            .orderBy(ApartmentsTable.createdAt, SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun findPending(): List<ApartmentResponse> = transaction {
        ApartmentsTable
            .selectAll().where { ApartmentsTable.status eq "PENDING" }
            .orderBy(ApartmentsTable.createdAt, SortOrder.ASC)
            .map { it.toResponse() }
    }

    fun approve(id: String, req: ApproveRequest): ApartmentResponse? = transaction {
        val account = req.accountNumber ?: generateAccountNumber()

        ApartmentsTable.update({ ApartmentsTable.id eq UUID.fromString(id) }) {
            it[status]        = "APPROVED"
            it[accountNumber] = account
            // УК может скорректировать адрес
            req.city?.let { v -> it[city] = v.trim() }
            req.street?.let { v -> it[street] = v.trim() }
            req.house?.let { v -> it[house] = v.trim() }
            req.building?.let { v -> it[building] = v.trim() }
            req.floor?.let { v -> it[floor] = v }
            req.apartment?.let { v -> it[apartment] = v.trim() }
            it[updatedAt] = Instant.now()
        }
        findById(id)
    }

    fun reject(id: String, note: String): ApartmentResponse? = transaction {
        ApartmentsTable.update({ ApartmentsTable.id eq UUID.fromString(id) }) {
            it[status]        = "REJECTED"
            it[rejectionNote] = note
            it[updatedAt]     = Instant.now()
        }
        findById(id)
    }

    private fun generateAccountNumber(): String {
        var account: String
        do {
            account = (1_000_000_000L..9_999_999_999L).random().toString()
        } while (accountExists(account))
        return account
    }

    private fun accountExists(account: String): Boolean = transaction {
        ApartmentsTable
            .select { ApartmentsTable.accountNumber eq account }
            .count() > 0
    }

    private fun ResultRow.toResponse() = ApartmentResponse(
        id            = this[ApartmentsTable.id].toString(),
        userId        = this[ApartmentsTable.userId],
        city          = this[ApartmentsTable.city],
        street        = this[ApartmentsTable.street],
        house         = this[ApartmentsTable.house],
        building      = this[ApartmentsTable.building],
        floor         = this[ApartmentsTable.floor],
        apartment     = this[ApartmentsTable.apartment],
        status        = this[ApartmentsTable.status],
        accountNumber = this[ApartmentsTable.accountNumber],
        rejectionNote = this[ApartmentsTable.rejectionNote],
        createdAt     = this[ApartmentsTable.createdAt].toString(),
        updatedAt     = this[ApartmentsTable.updatedAt].toString()
    )
}