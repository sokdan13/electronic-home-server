package com.example.data.repository

import com.example.data.table.RequestsTable
import com.example.domain.model.RequestCategory
import com.example.domain.model.RequestCreateDto
import com.example.domain.model.RequestResponse
import com.example.domain.model.RequestStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class RequestRepository {

    fun create(userId: String, dto: RequestCreateDto): RequestResponse = transaction {
        val id = RequestsTable.insert {
            it[this.userId]     = userId
            it[apartmentId]     = UUID.fromString(dto.apartmentId)
            it[category]        = dto.category
            it[description]     = dto.description
            it[status]          = RequestStatus.PENDING.name
            it[createdAt]       = Instant.now()
            it[updatedAt]       = Instant.now()
        } get RequestsTable.id
        findById(id.toString())!!
    }

    fun findById(id: String): RequestResponse? = transaction {
        RequestsTable
            .select { RequestsTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toResponse()
    }

    fun findByUser(userId: String): List<RequestResponse> = transaction {
        RequestsTable
            .select { RequestsTable.userId eq userId }
            .orderBy(RequestsTable.createdAt, SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun findAll(): List<RequestResponse> = transaction {
        RequestsTable
            .selectAll()
            .orderBy(RequestsTable.createdAt, SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun findByStatus(status: String): List<RequestResponse> = transaction {
        RequestsTable
            .select { RequestsTable.status eq status }
            .orderBy(RequestsTable.createdAt, SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun takeInProgress(id: String, dueDate: String): RequestResponse? = transaction {
        RequestsTable.update({ RequestsTable.id eq UUID.fromString(id) }) {
            it[status]          = RequestStatus.IN_PROGRESS.name
            it[RequestsTable.dueDate] = LocalDate.parse(dueDate)
            it[updatedAt]       = Instant.now()
        }
        findById(id)
    }

    fun markDone(id: String): RequestResponse? = transaction {
        RequestsTable.update({ RequestsTable.id eq UUID.fromString(id) }) {
            it[status]    = RequestStatus.DONE.name
            it[updatedAt] = Instant.now()
        }
        findById(id)
    }

    private fun ResultRow.toResponse(): RequestResponse {
        val cat    = RequestCategory.valueOf(this[RequestsTable.category])
        val status = RequestStatus.valueOf(this[RequestsTable.status])
        return RequestResponse(
            id            = this[RequestsTable.id].toString(),
            userId        = this[RequestsTable.userId],
            apartmentId   = this[RequestsTable.apartmentId].toString(),
            category      = cat.name,
            categoryLabel = cat.label,
            description   = this[RequestsTable.description],
            status        = status.name,
            statusLabel   = status.label,
            dueDate       = this[RequestsTable.dueDate]?.toString(),
            createdAt     = this[RequestsTable.createdAt].toString(),
            updatedAt     = this[RequestsTable.updatedAt].toString()
        )
    }
}