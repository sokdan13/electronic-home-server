package com.example.routes

import com.example.data.repository.MeterRepository
import com.example.domain.ApiResponse
import com.example.domain.model.MeterReadingRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.meterRoutes(repo: MeterRepository) {

    authenticate("resident") {

        post("/meters") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("sub").asString()
            val req = call.receive<MeterReadingRequest>()

            if (req.month !in 1..12) return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Некорректный месяц")
            )
            if (req.year < 2000) return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Некорректный год")
            )

            val result = repo.submit(userId, req)
            call.respond(HttpStatusCode.OK, ApiResponse(true, data = result))
        }

        get("/meters/{apartmentId}") {
            val apartmentId = call.parameters["apartmentId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Нет apartmentId")
            )
            val list = repo.findByApartment(apartmentId)
            call.respond(ApiResponse(true, data = list))
        }
    }
}