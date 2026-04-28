package com.example.routes

import com.example.data.repository.RequestRepository
import com.example.domain.ApiResponse
import com.example.domain.model.RequestCreateDto
import com.example.domain.model.RequestCategory
import com.example.domain.model.TakeInProgressDto
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.requestRoutes(repo: RequestRepository) {

    authenticate("resident") {

        post("/requests") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("user_id").asString()
            val dto = call.receive<RequestCreateDto>()

            if (dto.apartmentId.isBlank()) return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Укажите квартиру")
            )

            val validCategories = RequestCategory.entries.map { it.name }
            if (dto.category !in validCategories) return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Неверная категория")
            )

            val result = repo.create(userId, dto)
            call.respond(HttpStatusCode.Created, ApiResponse(true, data = result))
        }

        get("/requests/my") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("user_id").asString()
            call.respond(ApiResponse(true, data = repo.findByUser(userId)))
        }

        get("/requests/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Нет id")
            )
            val req = repo.findById(id) ?: return@get call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(false, error = "Заявка не найдена")
            )
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("user_id").asString()
            if (req.userId != userId) return@get call.respond(
                HttpStatusCode.Forbidden,
                ApiResponse<Nothing>(false, error = "Доступ запрещён")
            )
            call.respond(ApiResponse(true, data = req))
        }
    }

    authenticate("management") {

        get("/management/requests") {
            val status = call.request.queryParameters["status"]
            val list = if (status != null) repo.findByStatus(status) else repo.findAll()
            call.respond(ApiResponse(true, data = list))
        }

        patch("/management/requests/{id}/in-progress") {
            val id = call.parameters["id"] ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Нет id")
            )
            val dto = call.receive<TakeInProgressDto>()
            val result = repo.takeInProgress(id, dto.dueDate) ?: return@patch call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(false, error = "Заявка не найдена")
            )
            call.respond(ApiResponse(true, data = result))
        }

        patch("/management/requests/{id}/done") {
            val id = call.parameters["id"] ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(false, error = "Нет id")
            )
            val result = repo.markDone(id) ?: return@patch call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(false, error = "Заявка не найдена")
            )
            call.respond(ApiResponse(true, data = result))
        }
    }
}