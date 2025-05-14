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
            validate<CreateContractorDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<UpdateContractorDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        post({
            summary = "Создать новую бригаду"
            request {
                body<CreateContractorDto> { required = true }
            }
            response {
                code(HttpStatusCode.Created) {
                    body<ContractorDto>()
                    description = "Бригада успешна создана"
                }
                code(HttpStatusCode.BadRequest) {
                    body<String> {
                        example("Неправильная длина ") {
                            value = validateName("a")
                        }
                    }
                    description = "Запрос составлен не правильно"
                }
            }
        }) {
            val contractor = call.receive<CreateContractorDto>()
            val newContractor = contractorRepository.create(contractor)
            call.respond(HttpStatusCode.Created, newContractor)
        }

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
            route("/{contractorId}") {
                get({
                    summary = "Получить информацию о бригаде"
                    request {
                        pathParameter<Int>("contractorId") { required = true }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            body<ContractorDto>()
                            description = "Бригада найдена"
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
                    val id = call.parameters["contractorId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID format")
                    if (principalResult.id == id.toString() || principalResult.role != UserRole.ADMIN.name) {
                        val contractor = contractorRepository.findById(id)
                            ?: throw NotFoundException("Contractor not found")
                        call.respond(HttpStatusCode.OK, contractor)
                    }
                }

                put({
                    summary = "Обновить бригаду"
                    request {
                        pathParameter<Int>("contractorId") { required = true }
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
                                    value = validateName("a").joinToString()
                                }
                            }
                        }
                        code(HttpStatusCode.NotFound) {
                            description = "Такой бригады не найдено"
                        }
                    }
                }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val id = call.parameters["contractorId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID format")
                    val contractor = call.receive<UpdateContractorDto>()
                    if (principalResult.id == id.toString() || principalResult.role == UserRole.ADMIN.name) {
                        val result = contractorRepository.update(id, contractor)
                            ?: throw NotFoundException("Contract not found")
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        throw AccessForbiddenException("Access forbidden")
                    }
                }

                delete({
                    summary = "Удалить бригаду"
                    request {
                        pathParameter<Int>("contractorId") { required = true }
                    }
                    response {
                        code(HttpStatusCode.NoContent) {
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
                    val id = call.parameters["contractorId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID format")
                    if (principalResult.id == id.toString() || principalResult.role == UserRole.ADMIN.name) {
                        val result = contractorRepository.delete(id)
                        if (result) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            throw NotFoundException("Contractor with ID $id not found")
                        }
                    } else {
                        throw AccessForbiddenException("Access forbidden")
                    }
                }
            }
        }
    }
}
