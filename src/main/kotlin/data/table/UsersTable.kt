package com.example.data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val firebaseUid = varchar("firebase_uid", 128)
    val email       = varchar("email", 255)
    val firstName   = varchar("first_name", 100)
    val lastName    = varchar("last_name", 100)
    val createdAt   = timestamp("created_at")

    override val primaryKey = PrimaryKey(firebaseUid)
}