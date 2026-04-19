package com.example.plugins

import com.example.data.repository.ApartmentRepository
import com.example.data.repository.MeterRepository
import com.example.routes.apartmentRoutes
import com.example.routes.meterRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    val apartmentRepo = ApartmentRepository()
    val meterRepo = MeterRepository()

    routing {
        route("/api/v1") {
            apartmentRoutes(apartmentRepo)
            meterRoutes(meterRepo)
        }
    }
}
