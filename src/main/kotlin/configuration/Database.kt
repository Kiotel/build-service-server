package buildService.configuration

import buildService.features.contactors.ContractorsTable
import buildService.features.users.UsersTable
import buildService.features.workingSites.WorkingSiteContactorsTable
import buildService.features.workingSites.WorkingSitesTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureSchemas(database: Database) {
    transaction(database) {
        SchemaUtils.create(
            UsersTable,
            ContractorsTable,
            WorkingSitesTable,
            WorkingSiteContactorsTable
        )
    }
}