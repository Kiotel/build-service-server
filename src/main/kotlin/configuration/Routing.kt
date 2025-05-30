package buildService.configuration

import buildService.features.contactors.ContractorRepository
import buildService.features.contactors.comments.ContractorCommentsRepository
import buildService.features.contactors.contractorsRoutes
import buildService.features.useCases.CheckEmail
import buildService.features.users.UserRepository
import buildService.features.users.userRoutes
import buildService.features.workingSites.WorkingSiteRepository
import buildService.features.workingSites.workingSitesRoutes
import features.contactors.comments.contractorsCommentsRoutes
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository by inject<UserRepository>()
    val contractorRepository by inject<ContractorRepository>()
    val workingSiteRepository by inject<WorkingSiteRepository>()
    val contractorCommentsRepository by inject<ContractorCommentsRepository>()

    val checkEmail by inject<CheckEmail>()

    routing {
        userRoutes(userRepository, checkEmail)
        contractorsRoutes(contractorRepository, userRepository, checkEmail)
        workingSitesRoutes(workingSiteRepository)
        authRoutes(userRepository = userRepository, contractorRepository = contractorRepository)
        contractorsCommentsRoutes(
            contractorRepository = contractorRepository,
            contractorCommentsRepository = contractorCommentsRepository,
        )
        get("/") {
            call.respondText("hello changed")
        }
        authenticate("jwt") {
            get("/protected", {
                summary = "чисто тестовое, можно проверить токен"
            }) {
                val principal = call.principal<JWTPrincipal>()
                val email = principal?.payload?.getClaim("email")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()
                val id = principal?.payload?.getClaim("id")?.asString()
                call.respond(mapOf("id" to id, "email" to email, "role" to role))
            }
        }
    }
}

