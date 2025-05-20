package buildService.features.workingSites

import buildService.features.users.WorkingSiteRepository
import buildService.shared.utils.validateName
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.workingSitesRoutes(workingSiteRepository: WorkingSiteRepository) {
    route("/workingSites") {
        install(RequestValidation) {
            validate<UpdateWorkingSiteDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<CreateWorkingSiteDto> {
                val errors = validateName(it.name)
                if (it.userId < 0) errors.add("user id must be non-negative and not null")
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        post({
            summary = "Создать новый объект"
            request {
                body<CreateWorkingSiteDto> { required = true }
            }
            response {
                code(HttpStatusCode.Created) {
                    body<WorkingSiteDto>()
                    description = "Объект успешно создан"
                }
                code(HttpStatusCode.BadRequest) {
                    body<String> {
                        example("Неправильная длина имени") {
                            value = validateName("a").joinToString()
                        }
                    }
                    description = "Запрос составлен неправильно"
                }
            }
        }) {
            val workingSite = call.receive<CreateWorkingSiteDto>()
            val id = workingSiteRepository.create(workingSite)
            call.respond(HttpStatusCode.Created, id)
        }

        get({
            summary = "Получить все объекты"
            description = "В будущем будет добавлена пагинация"
            response {
                code(HttpStatusCode.OK) {
                    body<List<WorkingSiteDto>>()
                    description = "Успешно получен список объектов"
                }
            }
        }) {
            val workingSites = workingSiteRepository.findAll()
            call.respond(HttpStatusCode.OK, workingSites)
        }


        route("/{workingSiteId}") {
            get({
                summary = "Получить информацию об объекте"
                request {
                    pathParameter<Int>("workingSiteId") { required = true }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<WorkingSiteDto>()
                        description = "Объект найден"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Такой объект не найден"
                    }
                    code(HttpStatusCode.BadRequest) {
                        description = "Запрос составлен неправильно"
                    }
                }
            }) {
                val id =
                    call.parameters["workingSiteId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID")
                val workingSite = workingSiteRepository.findById(id)
                if (workingSite != null) {
                    call.respond(HttpStatusCode.OK, workingSite)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            put({
                summary = "Обновить объект"
                request {
                    pathParameter<Int>("workingSiteId") { required = true }
                    body<UpdateWorkingSiteDto> { required = true }
                }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Объект успешно обновлен"
                    }
                    code(HttpStatusCode.BadRequest) {
                        body<String> {
                            example("Неправильная длина имени") {
                                value = validateName("a").joinToString()
                            }
                        }
                        description = "Запрос составлен неправильно"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Такой объект не найден"
                    }
                }
            }) {
                val id =
                    call.parameters["workingSiteId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID")
                val workingSite = call.receive<UpdateWorkingSiteDto>()
                val result = workingSiteRepository.update(id, workingSite)
                call.respond(HttpStatusCode.OK, result)
            }

            delete({
                summary = "Удалить объект"
                request {
                    pathParameter<Int>("workingSiteId") { required = true }
                }
                response {
                    code(HttpStatusCode.NoContent) {
                        description = "Объект успешно удален"
                    }
                    code(HttpStatusCode.BadRequest) {
                        description = "Запрос составлен неправильно"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Такой объект не найден"
                    }
                }
            }) {
                val id =
                    call.parameters["workingSiteId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID")
                if (!workingSiteRepository.delete(id)) {
                    throw NotFoundException("Working site with ID $id not found")
                }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
