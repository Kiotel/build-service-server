package buildService.features.contactors

import buildService.features.users.UserDao
import buildService.features.users.UsersTable.email
import buildService.shared.utils.dbQuery
import io.ktor.server.plugins.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface ContractorRepository {
    suspend fun create(contractor: CreateContractor): ContractorDto
    suspend fun findAll(): List<ContractorDto>
    suspend fun findById(id: Int): ContractorDto?
    suspend fun findByEmail(findEmail: String): ContractorDao?
    suspend fun update(contractorId: Int, contractor: UpdateContractorDto): ContractorDto?
    suspend fun delete(contractorId: Int): Boolean
}

class ContractorRepositoryImpl() : ContractorRepository {

    override suspend fun create(contractor: CreateContractor): ContractorDto = dbQuery {
        val user = UserDao.findById(contractor.userId) ?: throw NotFoundException("User not found")
        ContractorDao.new {
            userId = user.id
            name = contractor.name
            workersAmount = contractor.workersAmount.coerceAtLeast(1)
            rating = 7f
        }.toDto()
    }

    override suspend fun findAll(): List<ContractorDto> {
        return dbQuery {
            ContractorDao.all().map(ContractorDao::toDto)
        }
    }

    override suspend fun findByEmail(findEmail: String): ContractorDao? {
        return dbQuery {
            val userId =
                UserDao.find { email eq findEmail }.singleOrNull()?.id ?: return@dbQuery null
            ContractorDao.findById(userId)
        }
    }

    override suspend fun findById(id: Int): ContractorDto? {
        return dbQuery {
            ContractorDao.findById(id)?.toDto()
        }
    }

    override suspend fun update(
        contractorId: Int, contractor: UpdateContractorDto
    ): ContractorDto? {
        return dbQuery {
            val entity = ContractorDao.findById(contractorId)
            entity?.apply {
                name = contractor.name
                user.email = contractor.email
                workersAmount = contractor.workersAmount.coerceAtLeast(1)
                rating = contractor.rating.coerceIn(0f, 10f)
                updatedAt = Clock.System.now()
            }
            entity?.toDto()
        }
    }

    override suspend fun delete(contractorId: Int): Boolean {
        return dbQuery {
            ContractorsTable.deleteWhere { id.eq(contractorId) }
        } > 0
    }
}