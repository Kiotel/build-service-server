package buildService.configuration

import buildService.features.contactors.ContractorsTable
import buildService.features.contactors.comments.ContractorCommentsTable
import buildService.features.users.UsersTable
import buildService.features.workingSites.WorkingSiteContractorsTable
import buildService.features.workingSites.WorkingSitesTable
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("UnusedReceiverParameter")
fun Application.configureSchemas(dotenv: Dotenv) {
    try {
        val database = Database.connect(
            url = "${dotenv["DB_DRIVER"]}://postgres:${dotenv["POSTGRES_INTERNAL_PORT"]}/${dotenv["POSTGRES_DB"]}",
            user = dotenv["POSTGRES_USER"],
            password = dotenv["POSTGRES_PASSWORD"]
        )
        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                ContractorsTable,
                WorkingSitesTable,
                WorkingSiteContractorsTable,
                ContractorCommentsTable
            )

        }
    } catch (_: Exception) {
        val database = Database.connect(
            url = "${dotenv["DB_DRIVER"]}://${dotenv["DB_URL"]}:${dotenv["POSTGRES_PUBLIC_PORT"]}/${dotenv["POSTGRES_DB"]}",
            user = dotenv["POSTGRES_USER"],
            password = dotenv["POSTGRES_PASSWORD"]
        )
        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                ContractorsTable,
                WorkingSitesTable,
                WorkingSiteContractorsTable,
                ContractorCommentsTable
            )
        }
    }
}
