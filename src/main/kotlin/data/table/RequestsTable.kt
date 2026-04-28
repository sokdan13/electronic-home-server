package com.example.data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object RequestsTable : Table("requests") {
    val id           = uuid("id").autoGenerate()
    val userId       = varchar("user_id", 128)
    val apartmentId  = uuid("apartment_id").references(ApartmentsTable.id)
    val category     = varchar("category", 50)
    val description  = text("description").nullable()
    val status       = varchar("status", 20).default("PENDING")
    val dueDate      = date("due_date").nullable()
    val createdAt    = timestamp("created_at")
    val updatedAt    = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}