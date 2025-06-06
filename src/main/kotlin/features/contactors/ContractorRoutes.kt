package buildService.features.contactors

import buildService.configuration.AccessForbiddenException
import buildService.configuration.UserRole
import buildService.features.useCases.CheckEmail
import buildService.features.users.RegisterUserDto
import buildService.features.users.UserRepository
import buildService.shared.utils.getInfo
import buildService.shared.utils.validateEmail
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

fun Route.contractorsRoutes(
    contractorRepository: ContractorRepository,
    userRepository: UserRepository,
    checkEmail: CheckEmail
) {
    route("/contractors") {
        install(RequestValidation) {
            validate<CreateContractorDto> {
                val errors = validateName(it.name)
                errors.addAll(validateEmail(it.email))
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<CreateContractorForUserDto> {
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
            if (checkEmail(contractor.email)) {
                throw BadRequestException("Email already in use")
            }
            val newUser = userRepository.create(
                RegisterUserDto(
                    name = contractor.name, email = contractor.email, password = contractor.password
                )
            )
            val newContractor = contractorRepository.create(
                contractor.toDomain(
                    userId = newUser.id
                )
            )
            call.respond(HttpStatusCode.Created, newContractor)
        }

        get({
            summary = "Получить все бригады"
            description = "Также пагинация будет когда-нибудь добавлена "
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
                    val updateContractorDto = call.receive<UpdateContractorDto>()
                    if (principalResult.id == id.toString() || principalResult.role == UserRole.ADMIN.name) {
                        if (principalResult.email != updateContractorDto.email) {
                            if (checkEmail(updateContractorDto.email)) {
                                throw BadRequestException("Email already in use")
                            }
                        }
                        val result = contractorRepository.update(id, updateContractorDto)
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
            post("/createForUser", {
                summary = "Создать новую бригаду для уже существующего пользователя"
                request {
                    pathParameter<Int>("userId") { required = true }
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
                val contractor = call.receive<CreateContractorForUserDto>()
                val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                if (contractorRepository.findById(principalResult.id.toInt()) != null) {
                    throw IllegalArgumentException("Contractor for this account already exists")
                }
                if (principalResult.role.uppercase() != UserRole.USER.name) {
                    throw BadRequestException("Token must be with role user")
                }
                val newContractor =
                    contractorRepository.create(
                        contractor.toDomain(
                            userId = principalResult.id.toInt(),
                            email = principalResult.email
                        )
                    )
                call.respond(HttpStatusCode.Created, newContractor)

            }
        }
    }
}
