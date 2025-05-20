package buildService.features.contactors

import buildService.shared.utils.dbQuery
import buildService.shared.utils.hashPassword
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface ContractorRepository {
    suspend fun create(contractor: CreateContractorDto): ContractorDto
    suspend fun findAll(): List<ContractorDto>
    suspend fun findById(id: Int): ContractorDto?
    suspend fun findByEmail(findEmail: String): ContractorDao?
    suspend fun update(contractorId: Int, contractor: UpdateContractorDto): ContractorDto?
    suspend fun delete(contractorId: Int): Boolean
}

class ContractorRepositoryImpl() : ContractorRepository {
    override suspend fun create(contractor: CreateContractorDto): ContractorDto = dbQuery {
        ContractorDao.new {
            name = contractor.name
            workersAmount = contractor.workersAmount.coerceAtLeast(1)
            email = contractor.email
            password = contractor.password.hashPassword()
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
            ContractorDao.find { ContractorsTable.email eq findEmail }.firstOrNull()
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
                email = contractor.email
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