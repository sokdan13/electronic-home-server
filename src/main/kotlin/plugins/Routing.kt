package com.example.plugins

import com.example.data.repository.ApartmentRepository
import com.example.routes.apartmentRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    val apartmentRepo = ApartmentRepository()

    routing {
        route("/api/v1") {
            apartmentRoutes(apartmentRepo)
        }
    }
}
