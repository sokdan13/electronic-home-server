package com.example.plugins

import com.example.data.repository.*
import com.example.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    val apartmentRepo = ApartmentRepository()
    val meterRepo = MeterRepository()
    val requestRepo = RequestRepository()
    val announcementRepo = AnnouncementRepository()
    val guestPassRepo = GuestPassRepository()



    routing {
        route("/api/v1") {
            apartmentRoutes(apartmentRepo)
            meterRoutes(meterRepo)
            requestRoutes(requestRepo)
            announcementRoutes(announcementRepo)
            guestPassRoutes(guestPassRepo)
        }
    }
}
