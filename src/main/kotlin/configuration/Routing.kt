package buildService.configuration

import buildService.features.contactors.ContractorRepository
import buildService.features.contactors.contractorsRoutes
import buildService.features.users.UserRepository
import buildService.features.users.WorkingSiteRepository
import buildService.features.users.userRoutes
import buildService.features.workingSites.workingSitesRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository by inject<UserRepository>()
    val contractorRepository by inject<ContractorRepository>()
    val workingSiteRepository by inject<WorkingSiteRepository>()
    routing {
        userRoutes(userRepository)
        contractorsRoutes(contractorRepository)
        workingSitesRoutes(workingSiteRepository)
    }
}
