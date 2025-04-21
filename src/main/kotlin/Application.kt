package buildService

import buildService.configuration.*
import buildService.di.configureDi
import io.ktor.server.application.*
import io.ktor.server.config.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module(config: ApplicationConfig = environment.config) {
    configureSecurity()
    configureStatusPages()
    configureHTTP()
    configureSerialization()
    configureContentNegotiation()
    configureAdministration()
    configureDi()
    configureSchemas(config)
    configureRouting()
}
