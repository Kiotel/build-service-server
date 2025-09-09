package buildService.configuration

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(OpenApi) {
        this.info {
            this.title = "API для BuildService"
            this.version = "0.1"
        }
        schemas {
            @Suppress("MISSING_DEPENDENCY_CLASS_IN_EXPRESSION_TYPE")
            this.generator = SchemaGenerator.kotlinx()
        }
        security {
            // configure a basic-auth security scheme
            securityScheme("MySecurityScheme") {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
            }
            // if no other security scheme is specified for a route, the one with this name is used instead
            defaultSecuritySchemeNames("MySecurityScheme")
            // if no other response is documented for "401 Unauthorized", this information is used instead
            defaultUnauthorizedResponse {
                description = "Не авторизован"
            }
        }
    }
    routing {
        route("api.json") {
            openApi()
        }
        route("swagger") {
            swaggerUI("/api.json")
        }
    }

}
