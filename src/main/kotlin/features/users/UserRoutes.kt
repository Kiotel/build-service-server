package buildService.features.users

import buildService.features.workingSites.WorkingSitesTable.user
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userRepository: UserRepository) {
    // Create user
    post("/users") {
        val user = call.receive<CreateUserDto>()
        val id = userRepository.create(user)
        call.respond(HttpStatusCode.Created, id)
    }

    // Read all users
    get("/users") {
        val users = userRepository.findAll()
        call.respond(HttpStatusCode.OK, users)
    }

    // Read user
    get("/users/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        val user = userRepository.findById(id)
        if (user != null) {
            call.respond(HttpStatusCode.OK, user)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    // Update user
    put("/users/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        val user = call.receiveNullable<UpdateUserDto>() ?: throw IllegalArgumentException("Invalid body")
        userRepository.update(id, user)
        call.respond(HttpStatusCode.OK)
    }

    // Delete user
    delete("/users/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        userRepository.delete(id)
        call.respond(HttpStatusCode.OK)
    }
}
