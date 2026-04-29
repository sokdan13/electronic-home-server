package com.example.data.repository

import com.example.data.table.AnnouncementsTable
import com.example.domain.model.AnnouncementCategory
import com.example.domain.model.AnnouncementResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class AnnouncementRepository {

    fun findAll(category: String? = null): List<AnnouncementResponse> = transaction {
        val query = if (category != null) {
            AnnouncementsTable.select { AnnouncementsTable.category eq category }
        } else {
            AnnouncementsTable.selectAll()
        }
        query
            .orderBy(AnnouncementsTable.createdAt, SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun findById(id: String): AnnouncementResponse? = transaction {
        AnnouncementsTable
            .selectAll().where { AnnouncementsTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toResponse()
    }

    private fun ResultRow.toResponse(): AnnouncementResponse {
        val cat = AnnouncementCategory.valueOf(this[AnnouncementsTable.category])
        return AnnouncementResponse(
            id            = this[AnnouncementsTable.id].toString(),
            title         = this[AnnouncementsTable.title],
            description   = this[AnnouncementsTable.description],
            category      = cat.name,
            categoryLabel = cat.label,
            imageUrl      = this[AnnouncementsTable.imageUrl],
            createdAt     = this[AnnouncementsTable.createdAt].toString(),
            updatedAt     = this[AnnouncementsTable.updatedAt].toString()
        )
    }
}