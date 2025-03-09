package buildService.features.users

import buildService.shared.utils.validateName
import io.ktor.http.*
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
            validate<CreateUserDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        // Create user
        post {
            val user = call.receive<CreateUserDto>()
            val id = userRepository.create(user)
            call.respond(HttpStatusCode.Created, id)
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
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
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
                userRepository.delete(id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
