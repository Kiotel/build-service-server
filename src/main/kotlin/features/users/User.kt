package buildService.features.users

import buildService.features.workingSites.WorkingSiteDao
import buildService.features.workingSites.WorkingSiteDto
import buildService.features.workingSites.WorkingSitesTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SizedIterable

object UsersTable : IntIdTable("users") {
    val name = varchar("name", length = 50)
    val email = varchar("email", length = 255).uniqueIndex()
    var password = varchar("password", length = 255)
}

class UserDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDao>(UsersTable)

    var name by UsersTable.name
    var email by UsersTable.email
    var password by UsersTable.password

    val workingSites: SizedIterable<WorkingSiteDao> by WorkingSiteDao referrersOn WorkingSitesTable orderBy WorkingSitesTable.id

    fun toDto() = UserDto(
        id = this.id.value,
        name = this.name,
        email = this.email,
        password = this.password,
        workingSites = this.workingSites.map { it.toDto() }
    )
}

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val workingSites: List<WorkingSiteDto>?
)

@Serializable
data class RegisterUserDto(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class UpdateUserDto(
    val id: Int,
    val name: String,
    val age: Int,
    val email: String
)