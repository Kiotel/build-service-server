package buildService

import buildService.configuration.*
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
