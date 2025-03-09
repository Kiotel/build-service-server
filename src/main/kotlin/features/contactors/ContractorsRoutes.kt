package buildService.features.contactors

import buildService.shared.utils.validateName
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.contractorsRoutes(contactorRepository: ContractorRepository) {
    route("/contractors") {
        install(RequestValidation) {
            validate<UpdateContractorDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
            validate<CreateContractorDto> {
                val errors = validateName(it.name)
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }

        // Create contractor
        post {
            val contractor = call.receive<CreateContractorDto>()
            val id = contactorRepository.create(contractor)
            call.respond(HttpStatusCode.Created, id)
        }

        // find all contractors
        get {
            val contractors = contactorRepository.findAll()
            call.respond(HttpStatusCode.OK, contractors)
        }

        // routes for specific contractor
        route("/{id}") {
            // Read contractor
            get {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val contractor = contactorRepository.findById(id)
                if (contractor != null) {
                    call.respond(HttpStatusCode.OK, contractor)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // Update contractor
            put {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val contractor = call.receive<UpdateContractorDto>()
                contactorRepository.update(id, contractor)
                call.respond(HttpStatusCode.OK)
            }

            // Delete contractor
            delete {
                val id =
                    call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                contactorRepository.delete(id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
