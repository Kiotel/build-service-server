package buildService.features.contactors

import buildService.features.workingSites.WorkingSiteContactorsTable.contractor
import io.ktor.http.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun validateName(name: String?): List<String> {
    val errors = mutableListOf<String>()
    if (name.isNullOrBlank()) {
        errors.add("Name cannot be blank or null")
    } else if (name.length !in 2..50) {
        errors.add("Name length must be from 2 to 50")
    }
    return errors
}

fun Route.contractorsRoutes(contactorRepository: ContractorRepository) {
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
    post("/contractors") {
        val contractor = call.receive<CreateContractorDto>()
        val id = contactorRepository.create(contractor)
        call.respond(HttpStatusCode.Created, id)
    }

    // Read all contractors
    get("/contractors") {
        val contractors = contactorRepository.findAll()
        call.respond(HttpStatusCode.OK, contractors)
    }

    // Read contractor
    get("/contractors/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        val contractor = contactorRepository.findById(id)
        if (contractor != null) {
            call.respond(HttpStatusCode.OK, contractor)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    // Update contractor
    put("/contractors/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        val contractor = call.receive<UpdateContractorDto>()
        contactorRepository.update(id, contractor)
        call.respond(HttpStatusCode.OK)
    }

    // Delete contractor
    delete("/contractors/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        contactorRepository.delete(id)
        call.respond(HttpStatusCode.OK)
    }
}
