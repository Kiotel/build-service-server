package buildService.features.users

import buildService.configuration.AccessForbiddenException
import buildService.configuration.UserRole
import buildService.shared.utils.getInfo
import buildService.shared.utils.validateEmail
import buildService.shared.utils.validateName
import buildService.shared.utils.validatePassword
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

fun Route.userRoutes(userRepository: UserRepository) {
    route("users") {
        install(RequestValidation) {
            validate<UpdateUserDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<RegisterUserDto> {
                val errors = validateName(it.name)
                errors.addAll(validateEmail(it.email))
                errors.addAll(validatePassword(it.password))
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        post({
            summary = "Создание пользователя"
            description = "Хотя правильнее назвать регистрация"
            request {
                body<RegisterUserDto> { required = true }
            }
            response {
                code(HttpStatusCode.Created) {
                    body<UserDto>()
                    description = "Пользователь успешно создан"
                }
                code(HttpStatusCode.BadRequest) {
                    body<String> {
                        example("Почта уже занята") {
                            value = "Email already in use"
                        }
                    }
                    description = "Почта уже занята или другая ошибка"
                }
            }
        }) {
            val user = call.receive<RegisterUserDto>()
            userRepository.findByEmail(user.email)?.let {
                throw BadRequestException("Email already in use")
            }
            val result = userRepository.create(user)
            call.respond(HttpStatusCode.Created, result)

        }

        // find all users
        get({
            summary = "Получение всех пользователей"
            description = "Позже сделаю пагинацию"
            response {
                code(HttpStatusCode.OK) {
                    body<UserDto>()
                    description = "Пользователи успешно получены"
                }
            }
        }) {
            val users = userRepository.findAll()
            call.respond(HttpStatusCode.OK, users)
        }

        // routes for specific user
        authenticate("jwt") {
            route("/{userId}") {
                // Read user
                get({
                    summary = "Получение информации о пользователе"
                    request {
                        pathParameter<Int>("userId") { required = true }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            body<UserDto>()
                            description = "Пользователь найден"
                        }
                        code(HttpStatusCode.Forbidden) {
                            description = "id не совпал с id пользователя"
                        }
                    }
                }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val id = call.parameters["userId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID")
                    if (principalResult.id == id.toString() || principalResult.role == UserRole.ADMIN.name) {
                        val user = userRepository.findById(id)
                            ?: throw NotFoundException("User with ID $id not found")
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        throw AccessForbiddenException("Access Forbidden")
                    }
                }

                // Update user
                put({
                    summary = "Обновление пользователя"
                    request {
                        body<UpdateUserDto> { required = true }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "Пользователь успешно обновлён"
                        }
                        code(HttpStatusCode.BadRequest) {
                            body<String> {
                                example("Почта уже занята") {
                                    value = "Email already in use"
                                }
                            }
                            description = "Ошибка при составлении запроса"
                        }
                        code(HttpStatusCode.Forbidden) {
                            description = "id не совпал с id пользователя"
                        }
                    }
                }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val id = call.parameters["userId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID")
                    val user = call.receiveNullable<UpdateUserDto>()
                        ?: throw BadRequestException("Invalid body")
                    if (principalResult.id == id.toString() || principalResult.role == UserRole.ADMIN.name) {
                        userRepository.update(id, user)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        throw AccessForbiddenException("Access Forbidden")
                    }
                }

                delete(
                    {
                        summary = "удаление пользователя"
                        request {
                            pathParameter<Int>("userId") { required = true }
                        }
                        response {
                            code(HttpStatusCode.OK) {
                                description = "Пользователь успешно обновлён"
                            }
                            code(HttpStatusCode.BadRequest) {
                                description = "Ошибка при составлении запроса"
                            }
                            code(HttpStatusCode.Forbidden) {
                                description = "id не совпал с id пользователя"
                            }
                        }
                    }) {
                    val principalResult = call.principal<JWTPrincipal>()!!.getInfo()
                    val id = call.parameters["userId"]?.toInt()
                        ?: throw BadRequestException("Invalid ID")
                    if (principalResult.role == UserRole.ADMIN.name || principalResult.id == id.toString()) {
                        if (userRepository.delete(id) == false) {
                            throw NotFoundException("User with ID $id not found")
                        }
                    } else {
                        throw AccessForbiddenException("Access denied")
                    }
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
