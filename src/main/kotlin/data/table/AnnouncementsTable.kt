package com.example.data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object AnnouncementsTable : Table("announcements") {
    val id          = uuid("id").autoGenerate()
    val title       = varchar("title", 255)
    val description = text("description")
    val category    = varchar("category", 20)
    val imageUrl    = varchar("image_url", 500).nullable()
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}