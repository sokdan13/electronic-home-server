package com.example.plugins


import com.example.data.table.ApartmentsTable
import com.example.data.table.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val dbUrl    = environment.config.property("database.url").getString()
    val driver   = environment.config.property("database.driver").getString()
    val user     = environment.config.property("database.user").getString()
    val password = environment.config.property("database.password").getString()

    val hikari = HikariDataSource(HikariConfig().apply {
        jdbcUrl              = dbUrl
        driverClassName      = driver
        username             = user
        this.password        = password
        maximumPoolSize      = 5
        isAutoCommit         = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    Database.connect(hikari)

    transaction {
        SchemaUtils.create(UsersTable, ApartmentsTable)
    }

    log.info("Database connected OK")
}