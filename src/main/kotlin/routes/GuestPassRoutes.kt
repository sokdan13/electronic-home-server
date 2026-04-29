package com.example.routes

import com.example.data.repository.GuestPassRepository
import com.example.domain.ApiResponse
import com.example.domain.model.GuestPassCreateDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.guestPassRoutes(repo: GuestPassRepository) {

    authenticate("resident") {

        post("/guest-passes") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("user_id").asString()
            val dto = call.receive<GuestPassCreateDto>()
            try {
                val pass = repo.create(userId, dto)
                call.respond(HttpStatusCode.Created, ApiResponse(true, data = pass))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, error = e.message))
            }
        }

        get("/guest-passes/my") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("user_id").asString()
            call.respond(ApiResponse(true, data = repo.findByUser(userId)))
        }

        get("/guest-passes/{token}") {
            val token = call.parameters["token"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Нет токена")
            )
            val pass = repo.findByToken(token) ?: return@get call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(false, error = "Пропуск не найден")
            )
            if (!pass.isValid) return@get call.respond(
                HttpStatusCode.Gone,
                ApiResponse<Nothing>(false, error = "Пропуск истёк")
            )
            call.respond(ApiResponse(true, data = pass))
        }
    }

    get("/guest-passes/validate/{token}") {
        val token  = call.parameters["token"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Nothing>(false, error = "Нет токена")
        )
        val result = repo.validate(token)
        call.respond(ApiResponse(true, data = result))
    }
}