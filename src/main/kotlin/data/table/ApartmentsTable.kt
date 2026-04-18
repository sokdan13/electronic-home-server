package com.example.data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object ApartmentsTable : Table("apartments") {
    val id            = uuid("id").autoGenerate()
    val userId        = varchar("user_id", 128)
    val city          = varchar("city", 100)
    val street        = varchar("street", 200)
    val house         = varchar("house", 20)
    val building      = varchar("building", 20).nullable()
    val floor         = integer("floor")
    val apartment     = varchar("apartment", 20)
    val status        = varchar("status", 20).default("PENDING")
    val accountNumber = varchar("account_number", 10).nullable()
    val rejectionNote = varchar("rejection_note", 500).nullable()
    val createdAt     = timestamp("created_at")
    val updatedAt     = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}