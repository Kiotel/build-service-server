@file:OptIn(ExperimentalTime::class)

package buildService.features.workingSites

import buildService.features.contactors.ContractorDao
import buildService.features.users.UserDao
import buildService.features.users.UsersTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object WorkingSitesTable : IntIdTable("working_sites") {
    val name = varchar("name", 255)
    val userId = reference("user_id", UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }
}

class WorkingSiteDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WorkingSiteDao>(WorkingSitesTable)

    var name by WorkingSitesTable.name
    var user by UserDao referencedOn WorkingSitesTable.userId
    var contractors by ContractorDao via WorkingSiteContractorsTable

    val createdAt by WorkingSitesTable.createdAt
    var updatedAt by WorkingSitesTable.updatedAt

    fun toDto() = WorkingSiteDto(
        id = this.id.value,
        name = this.name,
        userId = this.user.id.value,
        contractorsIds = this.contractors.map { it.id.value },
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )
}


@Serializable
data class WorkingSiteDto(
    val id: Int,
    val name: String,
    val userId: Int,
    val contractorsIds: List<Int>?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateWorkingSiteDto(
    val name: String, val userId: Int
)

@Serializable
data class UpdateWorkingSiteDto(
    val name: String?, val contractorsIds: List<Int>?
)