package buildService.configuration

import buildService.features.contactors.ContractorsTable
import buildService.features.users.UsersTable
import buildService.features.workingSites.WorkingSiteContractorsTable
import buildService.features.workingSites.WorkingSitesTable
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureSchemas(config: ApplicationConfig) {
    val database = Database.connect(
        url = config.property("storage.jdbcURL").getString(),
        user = config.property("storage.user").getString(),
        password = config.property("storage.password").getString()
    )
    transaction(database) {
        SchemaUtils.create(
            UsersTable,
            ContractorsTable,
            WorkingSitesTable,
            WorkingSiteContractorsTable
        )
    }
}