package buildService.features.workingSites

import buildService.features.contactors.ContractorDao
import buildService.features.users.UserDao
import buildService.features.users.UsersTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

object WorkingSitesTable : IntIdTable("working_sites") {
    val name = varchar("name", 255)
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
}

class WorkingSiteDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WorkingSiteDao>(WorkingSitesTable)

    var name by WorkingSitesTable.name
    var user by UserDao referencedOn WorkingSitesTable.userId
    var contractors by ContractorDao via WorkingSiteContractorsTable

    fun toDto() = WorkingSiteDto(
        id = this.id.value,
        name = this.name,
        userId = this.user.id.value,
        contractorsIds = this.contractors.map { it.id.value })
}


@Serializable
data class WorkingSiteDto(
    val id: Int,
    val name: String,
    val userId: Int,
    val contractorsIds: List<Int>?
)

@Serializable
data class CreateWorkingSiteDto(
    val name: String,
    val userId: Int
)

@Serializable
data class UpdateWorkingSiteDto(
    val name: String?,
    val contractorsIds: List<Int>?
)