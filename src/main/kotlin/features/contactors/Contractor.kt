package buildService.features.contactors

import buildService.features.contactors.comments.ContractorCommentDto
import buildService.features.contactors.comments.ContractorCommentsDao
import buildService.features.contactors.comments.ContractorCommentsTable
import buildService.features.users.UserDao
import buildService.features.users.UsersTable
import buildService.features.workingSites.WorkingSiteContractorsTable
import buildService.features.workingSites.WorkingSiteDao
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.charLength
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ContractorsTable : IntIdTable("contractors") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val name = varchar("name", 50).check { it.charLength().between(2, 50) }
    val workersAmount = integer("workers_amount").check { it greaterEq 1 }
    val rating = float("rating").check { it.between(0f, 10f) }
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Clock.System.now() }
}

class ContractorDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContractorDao>(ContractorsTable)

    var name by ContractorsTable.name
    var workersAmount by ContractorsTable.workersAmount
    var rating by ContractorsTable.rating

    var userId by ContractorsTable.userId
    val user by UserDao referencedOn ContractorsTable.userId

    val createdAt by ContractorsTable.createdAt
    var updatedAt by ContractorsTable.updatedAt

    var workingSites by WorkingSiteDao via WorkingSiteContractorsTable
    val comments by ContractorCommentsDao referrersOn ContractorCommentsTable.contractorId

    fun toDto() = ContractorDto(
        id = this.id.value,
        userId = this.userId.value,
        name = this.name,
        email = this.user.email,
        workersAmount = this.workersAmount,
        rating = this.rating,
        workingSitesIds = this.workingSites.map { it.id.value },
        comments = this.comments.map { it.toDto() },
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString(),
    )
}

@Serializable
data class ContractorDto(
    val id: Int,
    val userId: Int,
    val name: String,
    val email: String,
    val workersAmount: Int,
    val rating: Float,
    val workingSitesIds: List<Int>,
    val comments: List<ContractorCommentDto>,
    val createdAt: String,
    val updatedAt: String
)

data class CreateContractor(
    val name: String, val email: String, val workersAmount: Int, val userId: Int
)

@Serializable
data class CreateContractorDto(
    val name: String,
    val email: String,
    val workersAmount: Int,
    val password: String,
) {
    fun toDomain(userId: Int): CreateContractor {
        return CreateContractor(
            name = this.name,
            email = this.name,
            workersAmount = this.workersAmount,
            userId = userId
        )
    }
}

@Serializable
data class CreateContractorForUserDto(
    val name: String,
    val workersAmount: Int,
) {
    fun toDomain(userId: Int, email: String): CreateContractor {
        return CreateContractor(
            name = this.name,
            workersAmount = this.workersAmount,
            userId = userId,
            email = email
        )
    }
}

@Serializable
data class UpdateContractorDto(
    val name: String, val email: String, val workersAmount: Int, val rating: Float
)