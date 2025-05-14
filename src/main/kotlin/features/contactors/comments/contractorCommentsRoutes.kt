package features.contactors.comments

import buildService.configuration.AccessForbiddenException
import buildService.configuration.UserRole
import buildService.features.contactors.ContractorRepository
import buildService.features.contactors.comments.*
import buildService.shared.utils.getInfo
import buildService.shared.utils.validateText
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.contractorsCommentsRoutes(
    contractorCommentsRepository: ContractorCommentsRepository,
    contractorRepository: ContractorRepository
) {
    route("/comments") {
        install(RequestValidation) {
            validate<UpdateContractorCommentDto> {
                val errors = validateText(it.comment)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<CreateContractorCommentDto> {
                val errors = validateText(it.comment)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        route("/contractors/{contractorId}") {
            authenticate("jwt") {
                post({
                    summary = "Создать новый комментарий для указанной бригады"
                    request {
                        pathParameter<Int>("contractorId") {
                            required = true; description =
                            "ID бригады, к которой относится комментарий"
                        }
                        body<CreateContractorCommentDto> {
                            required = true; description = "Текст комментария"
                        }
                    }
                    response {
                        code(HttpStatusCode.Created) {
                            body<ContractorCommentDto>(); description = "Комментарий успешно создан"
                        }
                        code(HttpStatusCode.BadRequest) {
                            body<String> {
                                example("Невалидный текст комментария или неверный ID бригады") {
                                    value = "Invalid Contractor ID format in path"
                                }
                            }
                        }
                        code(HttpStatusCode.NotFound) {
                            description = "Бригада с указанным ID не найдена"
                        }
                        code(HttpStatusCode.Unauthorized) {
                            description = "Требуется аутентификация"
                        }
                    }
                }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val contractorId = call.parameters["contractorId"]?.toInt()
                        ?: throw BadRequestException("Invalid Contractor ID format in path")

                    contractorRepository.findById(contractorId)
                        ?: throw NotFoundException("Contractor with ID $contractorId not found")

                    val commentDtoFromBody = call.receive<CreateContractorCommentDto>()

                    val commentToCreate = CreateContractorComment(
                        contractorId = contractorId,
                        userId = principalResult.id.toInt(),
                        comment = commentDtoFromBody.comment
                    )

                    val newComment = contractorCommentsRepository.create(commentToCreate)
                    call.respond(HttpStatusCode.Created, newComment)
                }
            }
            get({
                summary = "Получить все комментарии для указанной бригады"
                request {
                    pathParameter<Int>("contractorId") {
                        required = true; description = "ID бригады"
                    }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<List<ContractorCommentDto>>(); description =
                        "Успешно получен список комментариев"
                    }
                    code(HttpStatusCode.BadRequest) { description = "Неверный ID бригады" }
                }
            }) {
                val contractorId = call.parameters["contractorId"]?.toInt()
                    ?: throw BadRequestException("Invalid Contractor ID format in path")
                val comments = contractorCommentsRepository.findAllForContractor(contractorId)
                call.respond(HttpStatusCode.OK, comments)
            }
        }
        get("/user/{userId}", {
            summary = "Получить все комментарии указанного пользователя"
            request {
                pathParameter<Int>("userId") {
                    required = true; description = "ID пользователя"
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<List<ContractorCommentDto>>(); description =
                    "Успешно получен список комментариев"
                }
                code(HttpStatusCode.BadRequest) { description = "Неверный ID пользователя" }
            }
        }) {
            val userId = call.parameters["userId"]?.toInt()
                ?: throw BadRequestException("Invalid User ID format in path")
            val comments = contractorCommentsRepository.findAllForUser(userId)
            call.respond(HttpStatusCode.OK, comments)
        }

        authenticate("jwt") {
            put("/{commentId}", {
                summary = "Обновить комментарий по ID"
                request {
                    pathParameter<Int>("commentId") {
                        required = true; description = "ID комментария для обновления"
                    }
                    body<UpdateContractorCommentDto> {
                        required = true; description = "Новый текст комментария"
                    }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<ContractorCommentDto>(); description = "Комментарий успешно обновлен"
                    }
                    code(HttpStatusCode.BadRequest) {
                        body<String> {
                            example("Невалидный текст комментария или неверный ID") {
                                value = "Invalid Comment ID format in path"
                            }
                        }
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Комментарий с указанным ID не найден"
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Нет прав на обновление этого комментария"
                    }
                    code(HttpStatusCode.Unauthorized) {
                        description = "Требуется аутентификация"
                    }
                }
            }) {
                val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                val commentId = call.parameters["commentId"]?.toInt()
                    ?: throw BadRequestException("Invalid Comment ID format in path")

                val updateDto = call.receive<UpdateContractorCommentDto>()

                val foundComment = contractorCommentsRepository.findById(commentId)
                    ?: throw NotFoundException("Comment with ID $commentId not found")

                if (principalResult.id == foundComment.userId.toString() || principalResult.role == UserRole.ADMIN.name) {
                    val updatedComment = contractorCommentsRepository.update(commentId, updateDto)
                        ?: throw NotFoundException("Comment with ID $commentId not found during update attempt") // Should be rare
                    call.respond(HttpStatusCode.OK, updatedComment)
                } else {
                    throw AccessForbiddenException("User not authorized to update comment $commentId")
                }
            }

            delete("/{commentId}", {
                summary = "Удалить комментарий по ID"
                request {
                    pathParameter<Int>("commentId") {
                        required = true; description = "ID комментария для удаления"
                    }
                }
                response {
                    code(HttpStatusCode.NoContent) {
                        description = "Комментарий успешно удален"
                    } // Use 204 No Content
                    code(HttpStatusCode.BadRequest) { description = "Неверный ID комментария" }
                    code(HttpStatusCode.NotFound) {
                        description = "Комментарий с указанным ID не найден"
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Нет прав на удаление этого комментария"
                    }
                }
            }) {
                val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                val commentId = call.parameters["commentId"]?.toInt()
                    ?: throw BadRequestException("Invalid Comment ID format in path")

                val foundComment = contractorCommentsRepository.findById(commentId)
                    ?: throw NotFoundException("Comment with ID $commentId not found")

                if (principalResult.id == foundComment.userId.toString() || principalResult.role == UserRole.ADMIN.name) {
                    val deleted = contractorCommentsRepository.delete(commentId)
                    if (!deleted) {
                        throw NotFoundException("Comment with ID $commentId could not be deleted (already gone?)")
                    }
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    throw AccessForbiddenException("User not authorized to delete comment $commentId")
                }
            }
        }
    }
}
