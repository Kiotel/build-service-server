package buildService.features.users

import buildService.shared.utils.validateEmail
import buildService.shared.utils.validateName
import buildService.shared.utils.validatePassword
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userRepository: UserRepository) {
    route("/users") {
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

        // Create user
        post {
            val user = call.receive<RegisterUserDto>()
            userRepository.findByEmail(user.email)?.let {
                throw BadRequestException("Email already in use")
            }
            val result = userRepository.create(user)
            call.respond(HttpStatusCode.Created, result)

        }

        // find all users
        get {
            val users = userRepository.findAll()
            call.respond(HttpStatusCode.OK, users)
        }


        // routes for specific user
        route("/{id}") {
            // Read user
            get {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val user = userRepository.findById(id)
                    ?: throw NotFoundException("User with ID $id not found")
                call.respond(HttpStatusCode.OK, user)
            }

            // Update user
            put {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val user = call.receiveNullable<UpdateUserDto>()
                    ?: throw IllegalArgumentException("Invalid body")
                userRepository.update(id, user)
                call.respond(HttpStatusCode.OK)
            }

            // Delete user
            delete {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                if (userRepository.delete(id) == false) {
                    throw NotFoundException("User with ID $id not found")
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
