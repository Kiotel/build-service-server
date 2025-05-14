package buildService

import buildService.configuration.*
import buildService.di.configureDi
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.config.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module(config: ApplicationConfig = environment.config) {
    val isTesting = config.property("isTesting").getString() == "true"
    val dotenv = dotenv {
        this.filename =
            if (isTesting) "test.env" else "prod.env"
    }
    configureSecurity(dotenv)
    configureStatusPages()
    configureHTTP()
    configureContentNegotiation()
    configureAdministration()
    configureDi()
    configureSchemas(dotenv)
    configureRouting()
}

// Список дел:
// Составить документацию;
// Доделать бд;
// Доделать пути;
// Сделать пагинацию;
// Сделать кэширование;