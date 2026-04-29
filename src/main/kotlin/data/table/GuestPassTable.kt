package com.example.data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object GuestPassTable : Table("guest_passes") {
    val id          = uuid("id").autoGenerate()
    val userId      = varchar("user_id", 128)
    val apartmentId = uuid("apartment_id").references(ApartmentsTable.id)
    val token       = varchar("token", 64).uniqueIndex()
    val expiresAt   = timestamp("expires_at")
    val createdAt   = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}