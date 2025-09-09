package buildService.features.contactors.comments

import buildService.features.contactors.ContractorDao
import buildService.features.users.UserDao
import buildService.shared.utils.dbQuery
import io.ktor.server.plugins.*
import kotlinx.datetime.Clock.System
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

data class CreateContractorComment(
    val contractorId: Int, val userId: Int, val comment: String
)

interface ContractorCommentsRepository {
    suspend fun create(newComment: CreateContractorComment): ContractorCommentDto
    suspend fun findAllForContractor(contractorId: Int): List<ContractorCommentDto>
    suspend fun findAllForUser(userId: Int): List<ContractorCommentDto>
    suspend fun findById(commentId: Int): ContractorCommentDto?
    suspend fun update(
        commentId: Int, updateComment: UpdateContractorCommentDto
    ): ContractorCommentDto?

    suspend fun delete(commentId: Int): Boolean
}

class ContractorCommentsRepositoryImpl() : ContractorCommentsRepository {
    override suspend fun create(
        newComment: CreateContractorComment
    ): ContractorCommentDto {
        return dbQuery {
            val contractor = ContractorDao.findById(newComment.contractorId)
                ?: throw NotFoundException("Contractor with id ${newComment.contractorId} not found")
            val user = UserDao.findById(newComment.userId)
                ?: throw NotFoundException("User with id ${newComment.userId} not found")

            ContractorCommentsDao.new {
                this.contractor = contractor
                this.user = user
                this.comment = newComment.comment
            }.toDto()
        }
    }

    override suspend fun findAllForUser(userId: Int): List<ContractorCommentDto> {
        return dbQuery {
            ContractorCommentsDao.find { ContractorCommentsTable.userId eq userId }
                .map { it.toDto() }
        }
    }

    override suspend fun findAllForContractor(contractorId: Int): List<ContractorCommentDto> {
        return dbQuery {
            ContractorCommentsDao.find { ContractorCommentsTable.contractorId eq contractorId }
                .map { it.toDto() }
        }
    }

    override suspend fun findById(commentId: Int): ContractorCommentDto? {
        return dbQuery {
            ContractorCommentsDao.findById(commentId)?.toDto()
        }
    }

    override suspend fun update(
        commentId: Int, updateComment: UpdateContractorCommentDto
    ): ContractorCommentDto? {
        return dbQuery {
            val comment = ContractorCommentsDao.findById(commentId)
            comment?.apply {
                this.comment = updateComment.comment
                updatedAt = System.now()
                isChanged = true
            }
            comment?.toDto()
        }
    }

    override suspend fun delete(commentId: Int): Boolean {
        return dbQuery {
            ContractorCommentsTable.deleteWhere { id.eq(commentId) }
        } > 0
    }
}