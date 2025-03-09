package buildService.configuration

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.SerializationException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.postgresql.util.PSQLException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message ?: "Unknown error")
        }
        exception<ExposedSQLException> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Database constraint violation")
            )
        }
    }
}
