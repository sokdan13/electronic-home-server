package com.example.data.repository

import com.example.data.table.MetersTable
import com.example.domain.model.MeterReadingRequest
import com.example.domain.model.MeterReadingResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class MeterRepository {

    fun submit(userId: String, req: MeterReadingRequest): MeterReadingResponse = transaction {
        val existing = MetersTable
            .selectAll().where {
                (MetersTable.apartmentId eq UUID.fromString(req.apartmentId)) and
                        (MetersTable.month eq req.month) and
                        (MetersTable.year eq req.year)
            }
            .singleOrNull()

        if (existing != null) {
            MetersTable.update({
                (MetersTable.apartmentId eq UUID.fromString(req.apartmentId)) and
                        (MetersTable.month eq req.month) and
                        (MetersTable.year eq req.year)
            }) {
                req.hotWater?.let  { v -> it[hotWater]  = BigDecimal.valueOf(v) }
                req.coldWater?.let { v -> it[coldWater] = BigDecimal.valueOf(v) }
                req.heating?.let   { v -> it[heating]   = BigDecimal.valueOf(v) }
                req.elecDay?.let   { v -> it[elecDay]   = BigDecimal.valueOf(v) }
                req.elecNight?.let { v -> it[elecNight] = BigDecimal.valueOf(v) }
                req.elecPeak?.let  { v -> it[elecPeak]  = BigDecimal.valueOf(v) }
                it[updatedAt] = Instant.now()
            }
            MetersTable
                .selectAll().where {
                    (MetersTable.apartmentId eq UUID.fromString(req.apartmentId)) and
                            (MetersTable.month eq req.month) and
                            (MetersTable.year eq req.year)
                }
                .single()
                .toResponse()
        } else {
            val id = MetersTable.insert {
                it[this.userId]      = userId
                it[apartmentId]      = UUID.fromString(req.apartmentId)
                it[month]            = req.month
                it[year]             = req.year
                it[hotWater]         = req.hotWater?.let  { v -> BigDecimal.valueOf(v) }
                it[coldWater]        = req.coldWater?.let { v -> BigDecimal.valueOf(v) }
                it[heating]          = req.heating?.let   { v -> BigDecimal.valueOf(v) }
                it[elecDay]          = req.elecDay?.let   { v -> BigDecimal.valueOf(v) }
                it[elecNight]        = req.elecNight?.let { v -> BigDecimal.valueOf(v) }
                it[elecPeak]         = req.elecPeak?.let  { v -> BigDecimal.valueOf(v) }
                it[createdAt]        = Instant.now()
                it[updatedAt]        = Instant.now()
            } get MetersTable.id
            findById(id.toString())!!
        }
    }

    fun findByApartment(apartmentId: String): List<MeterReadingResponse> = transaction {
        MetersTable
            .selectAll().where { MetersTable.apartmentId eq UUID.fromString(apartmentId) }
            .orderBy(MetersTable.year to SortOrder.DESC, MetersTable.month to SortOrder.DESC)
            .map { it.toResponse() }
    }

    fun findById(id: String): MeterReadingResponse? = transaction {
        MetersTable
            .selectAll().where { MetersTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toResponse()
    }

    private fun ResultRow.toResponse() = MeterReadingResponse(
        id          = this[MetersTable.id].toString(),
        apartmentId = this[MetersTable.apartmentId].toString(),
        month       = this[MetersTable.month],
        year        = this[MetersTable.year],
        hotWater    = this[MetersTable.hotWater]?.toDouble(),
        coldWater   = this[MetersTable.coldWater]?.toDouble(),
        heating     = this[MetersTable.heating]?.toDouble(),
        elecDay     = this[MetersTable.elecDay]?.toDouble(),
        elecNight   = this[MetersTable.elecNight]?.toDouble(),
        elecPeak    = this[MetersTable.elecPeak]?.toDouble(),
        createdAt   = this[MetersTable.createdAt].toString(),
        updatedAt   = this[MetersTable.updatedAt].toString()
    )
}