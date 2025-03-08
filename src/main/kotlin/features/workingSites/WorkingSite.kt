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

@Serializable
class WorkingSiteDto(val id: Int, val userId: Int, val contractorsIds: List<Int>)

object WorkingSitesTable : IntIdTable("working_sites") { // Lowercase
    val user = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
}

class WorkingSiteDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WorkingSiteDAO>(WorkingSitesTable)

    val user: UserDao by UserDao referencedOn WorkingSitesTable.user
    val contractors: SizedIterable<ContractorDao> by ContractorDao via WorkingSiteContactorsTable

    fun toDto() = WorkingSiteDto(
        id = this.id.value,
        userId = this.user.id.value,
        contractorsIds = this.contractors.map { it.id.value }
    )
}

