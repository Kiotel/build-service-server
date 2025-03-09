package buildService.features.workingSites

import buildService.features.contactors.UpdateContractorDto
import buildService.features.users.WorkingSiteRepository
import buildService.shared.utils.validateName
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.workingSitesRoutes(workingSiteRepository: WorkingSiteRepository) {
    route("/workingSites") {
        install(RequestValidation) {
            validate<UpdateWorkingSiteDto> {
                var errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<CreateWorkingSiteDto> {
                var errors = validateName(it.name)
                if (it.userId < 0) errors.add("user id must be non-negative and not null")
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        // Create contractor
        post {
            val workingSite = call.receive<CreateWorkingSiteDto>()
            val id = workingSiteRepository.create(workingSite)
            call.respond(HttpStatusCode.Created, id)
        }

        // find all workingSites
        get {
            val workingSites = workingSiteRepository.findAll()
            call.respond(HttpStatusCode.OK, workingSites)
        }


        // routes for specific workingSite
        route("/{id}") {
            // Find workingSite by id
            get {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val workingSite = workingSiteRepository.findById(id)
                if (workingSite != null) {
                    call.respond(HttpStatusCode.OK, workingSite)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // Update workingSite
            put {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val workingSite = call.receive<UpdateWorkingSiteDto>()
                workingSiteRepository.update(id, workingSite)
                call.respond(HttpStatusCode.OK)
            }

            // Delete workingSite
            delete {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                workingSiteRepository.delete(id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
