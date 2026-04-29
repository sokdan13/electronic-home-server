package com.example.routes

import com.example.data.repository.AnnouncementRepository
import com.example.domain.ApiResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.announcementRoutes(repo: AnnouncementRepository) {

    get("/announcements") {
        val category = call.request.queryParameters["category"]
        call.respond(ApiResponse(true, data = repo.findAll(category)))
    }

    get("/announcements/{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            io.ktor.http.HttpStatusCode.BadRequest,
            ApiResponse<Nothing>(false, error = "Нет id")
        )
        val item = repo.findById(id) ?: return@get call.respond(
            io.ktor.http.HttpStatusCode.NotFound,
            ApiResponse<Nothing>(false, error = "Не найдено")
        )
        call.respond(ApiResponse(true, data = item))
    }
}