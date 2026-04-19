package com.example.data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object MetersTable : Table("meter_readings") {
    val id          = uuid("id").autoGenerate()
    val userId      = varchar("user_id", 128)
    val apartmentId = uuid("apartment_id").references(ApartmentsTable.id)
    val month       = integer("month")
    val year        = integer("year")
    val hotWater    = decimal("hot_water", 10, 3).nullable()
    val coldWater   = decimal("cold_water", 10, 3).nullable()
    val heating     = decimal("heating", 10, 3).nullable()
    val elecDay     = decimal("elec_day", 10, 3).nullable()
    val elecNight   = decimal("elec_night", 10, 3).nullable()
    val elecPeak    = decimal("elec_peak", 10, 3).nullable()
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("unique_reading", apartmentId, month, year)
    }
}