package buildService

import buildService.configuration.configureAdministration
import buildService.configuration.configureContentNegotiation
import buildService.configuration.configureHTTP
import buildService.configuration.configureRouting
import buildService.configuration.configureSchemas
import buildService.configuration.configureSecurity
import buildService.configuration.configureSerialization
import buildService.configuration.configureStatusPages
import buildService.di.configureDi
import io.ktor.server.application.*
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureStatusPages()
    configureHTTP()
    configureSerialization()
    configureContentNegotiation()
    configureAdministration()
    configureDi(environment.config)
    configureSchemas(get())
    configureRouting()
}
