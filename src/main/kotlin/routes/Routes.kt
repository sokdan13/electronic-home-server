package com.example.routes


import com.example.data.repository.ApartmentRepository
import com.example.domain.ApartmentRequest
import com.example.domain.ApiResponse
import com.example.domain.ApproveRequest
import com.example.domain.RejectRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.apartmentRoutes(repo: ApartmentRepository) {


    authenticate("resident") {


        post("/apartments") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("sub").asString()
            val req = call.receive<ApartmentRequest>()


            if (req.city.isBlank() || req.street.isBlank() ||
                req.house.isBlank() || req.apartment.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, error = "Заполните все обязательные поля")
                )
            }
            if (req.floor < 1 || req.floor > 200) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, error = "Некорректный этаж")
                )
            }

            val apartment = repo.create(userId, req)
            call.respond(HttpStatusCode.Created, ApiResponse(true, data = apartment))
        }


        get("/apartments/my") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("uid").asString()
            val list = repo.findByUserId(userId)
            call.respond(ApiResponse(true, data = list))
        }


        get("/apartments/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, error = "Нет id")
                )

            val apt = repo.findById(id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, error = "Квартира не найдена")
                )

            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("uid").asString()
            if (apt.userId != userId) {
                return@get call.respond(
                    HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, error = "Доступ запрещён")
                )
            }

            call.respond(ApiResponse(true, data = apt))
        }
    }

    // УК — управление заявками (отдельная роль)
    authenticate("management") {

        // Все заявки (или только PENDING)
        get("/management/apartments") {
            val status = call.request.queryParameters["status"]
            val list = if (status == "PENDING") repo.findPending()
            else repo.findAll()
            call.respond(ApiResponse(true, data = list))
        }

        patch("/management/apartments/{id}/approve") {
            val id = call.parameters["id"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, error = "Нет id")
                )
            val req = call.receive<ApproveRequest>()
            val apt = repo.approve(id, req)
                ?: return@patch call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, error = "Квартира не найдена")
                )
            call.respond(ApiResponse(true, data = apt))
        }

        patch("/management/apartments/{id}/reject") {
            val id = call.parameters["id"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, error = "Нет id")
                )
            val req = call.receive<RejectRequest>()
            val apt = repo.reject(id, req.note)
                ?: return@patch call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, error = "Квартира не найдена")
                )
            call.respond(ApiResponse(true, data = apt))
        }
    }
}