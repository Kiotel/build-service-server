package buildService.features.users

import buildService.features.workingSites.WorkingSiteDAO
import buildService.features.workingSites.WorkingSiteDto
import buildService.features.workingSites.WorkingSitesTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SizedIterable

object UsersTable : IntIdTable("users") {
    val name = varchar("name", length = 50)
    val age = integer("age")
}

class UserDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDao>(UsersTable)

    var name by UsersTable.name
    var age by UsersTable.age

    val workingSites: SizedIterable<WorkingSiteDAO> by WorkingSiteDAO referrersOn WorkingSitesTable orderBy WorkingSitesTable.id

    fun toDto() = UserDto(
        id = this.id.value,
        name = this.name,
        age = this.age,
        workingSites = this.workingSites.map { it.toDto() }
    )
}

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val age: Int,
    val workingSites: List<WorkingSiteDto>?
)

@Serializable
data class CreateUserDto(
    val name: String,
    val age: Int,
)

@Serializable
data class UpdateUserDto(
    val name: String,
    val age: Int,
)