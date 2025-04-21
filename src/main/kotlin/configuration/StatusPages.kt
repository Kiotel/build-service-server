package buildService.configuration

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to cause.reasons.joinToString(",\n"))
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
        exception<ExposedSQLException> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Database constraint violation")
            )
        }
        exception<AccessForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden, mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }
}

class AccessForbiddenException(message: String) : Throwable(message)