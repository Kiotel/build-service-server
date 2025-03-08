package buildService.configuration

import buildService.features.contactors.ContractorRepository
import buildService.features.contactors.contractorsRoutes
import buildService.features.users.UserRepository
import buildService.features.users.userRoutes
import buildService.features.users.UserRepositoryImpl
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository by inject<UserRepository>()
    val contractorRepository by inject<ContractorRepository>()
    routing {
        userRoutes(userRepository)
        contractorsRoutes(contractorRepository)

        get("/") {
            call.respondText("Hello World!")
        }
    }
}
