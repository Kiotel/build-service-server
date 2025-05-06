package buildService.features.contactors.comments

import buildService.features.contactors.ContractorDao
import buildService.features.contactors.ContractorsTable
import buildService.features.users.UserDao
import buildService.features.users.UsersTable
import kotlinx.datetime.Clock.System
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ContractorCommentsTable : IntIdTable("contractor_comments") {
    val contractorId =
        reference("contractor_id", ContractorsTable, onDelete = ReferenceOption.CASCADE)
    val userId = optReference("user_id", UsersTable, onDelete = ReferenceOption.SET_NULL)
    val comment = text("comment")
    val isChanged = bool("is_changed").default(false)
    val createdAt = timestamp("created_at").clientDefault { System.now() }
    val updateAt = timestamp("updated_at").clientDefault { System.now() }
}

class ContractorCommentsDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContractorCommentsDao>(ContractorCommentsTable)

    var user by UserDao optionalReferencedOn ContractorCommentsTable.userId
    var contractor by ContractorDao referencedOn ContractorCommentsTable.contractorId
    var comment by ContractorCommentsTable.comment
    var isChanged by ContractorCommentsTable.isChanged
    var createdAt by ContractorCommentsTable.createdAt
    var updatedAt by ContractorCommentsTable.updateAt

    fun toDto() = ContractorCommentDto(
        commentId = this.id.value,
        contractorId = this.contractor.id.value,
        userId = this.user?.id?.value,
        comment = this.comment,
        isChanged = this.isChanged,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString(),
    )
}

@Serializable
data class ContractorCommentDto(
    val commentId: Int,
    val contractorId: Int,
    val userId: Int?,
    val comment: String,
    val isChanged: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreateContractorCommentDto(
    val comment: String
)

@Serializable
data class UpdateContractorCommentDto(
    val comment: String
)