package buildService.features.contactors

import buildService.configuration.AccessForbiddenException
import buildService.configuration.UserRole
import buildService.shared.utils.getInfo
import buildService.shared.utils.validateName
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

fun Route.contractorsRoutes(contractorRepository: ContractorRepository) {
    route("/contractors") {
        install(RequestValidation) {
            validate<UpdateContractorDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<RegisterContractorDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        // Create contractor
        post({
            summary = "Создать новую бригаду"
            request {
                body<RegisterContractorDto> { required = true }
            }
            response {
                code(HttpStatusCode.Created) {
                    body<ContractorDto>()
                    description = "Бригада успешна создана"
                }
                code(HttpStatusCode.BadRequest) {
                    body<String> {
                        example("Неправильная длина имени") {
                            value = "Name length must be from 2 to 50"
                        }
                    }
                    description = "Запрос составлен не правильно"
                }
            }
        }) {
            val contractor = call.receive<RegisterContractorDto>()
            val newContractor = contractorRepository.create(contractor)
            call.respond(HttpStatusCode.Created, newContractor)
        }

        // Find all contractors
        get({
            summary = "Получить все бригады"
            description = "Также пагинация будет добавлена"
            response {
                code(HttpStatusCode.OK) {
                    body<List<ContractorDto>>()
                    description = "Успешно получен список бригад"
                }
            }
        }) {
            val contractors = contractorRepository.findAll()
            call.respond(HttpStatusCode.OK, contractors)
        }

        authenticate("jwt") {
            // Routes for specific contractor
            route("/{id}") {
                // Read contractor
                get({
                    summary = "Получить информацию о бригаде"
                    request {
                        pathParameter<Int>("id") { required = true }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            body<ContractorDto>()
                            description = "Бригида найдена"
                        }
                        code(HttpStatusCode.NotFound) {
                            description = "Такой бригады не найдено"
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "Запрос составлен неправильно"
                        }
                        code(HttpStatusCode.Forbidden) {
                            description = "Доступ к этой бригаде запрещен"
                        }
                    }
                }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val id = call.parameters["id"]?.toInt()
                        ?: throw BadRequestException("Invalid ID format")
                    if (principalResult.id == id.toString() || principalResult.role != UserRole.ADMIN.name) {
                        val contractor = contractorRepository.findById(id)
                            ?: throw NotFoundException("Contractor not found")
                        call.respond(HttpStatusCode.OK, contractor)
                    }
                }

                // Update contractor
                put({
                    summary = "Обновить бригаду"
                    request {
                        pathParameter<Int>("id") { required = true }
                        body<UpdateContractorDto> { required = true }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "Бригада успешно обновлена"
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "Запрос составлен неправильно"
                            body<String> {
                                example("Неправильная длина имени") {
                                    value = "Name length must be from 2 to 50"
                                }
                            }
                        }
                        code(HttpStatusCode.NotFound) {
                            description = "Такой бригады не найдено"
                        }
                    }
                }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val id = call.parameters["id"]?.toInt()
                        ?: throw BadRequestException("Invalid ID format")
                    val contractor = call.receive<UpdateContractorDto>()
                    if (principalResult.id == id.toString() || principalResult.role == UserRole.ADMIN.name) {
                        contractorRepository.update(id, contractor)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        throw AccessForbiddenException("Access forbidden")
                    }
                }

                // Delete contractor
                delete({
                    summary = "Удалить бригаду"
                    request {
                        pathParameter<Int>("id") { required = true }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "Бригада успешно удалена"
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "Запрос составлен неправильно"
                        }
                        code(HttpStatusCode.NotFound) {
                            description = "Такая бригада не найдена"
                        }
                    }
                }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val id = call.parameters["id"]?.toInt()
                        ?: throw BadRequestException("Invalid ID format")
                    if (principalResult.id == id.toString() || principalResult.role == UserRole.ADMIN.name) {
                        contractorRepository.delete(id)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        throw AccessForbiddenException("Access forbidden")
                    }
                }
            }
        }
    }
}