package buildService.features.contactors

import buildService.shared.utils.dbQuery
import buildService.shared.utils.hashPassword
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface ContractorRepository {
    suspend fun create(user: CreateContractorDto): ContractorDto
    suspend fun findAll(): List<ContractorDto>
    suspend fun findById(id: Int): ContractorDto?
    suspend fun findByEmail(email: String): ContractorDao?
    suspend fun update(id: Int, contractorDto: UpdateContractorDto): ContractorDto?
    suspend fun delete(id: Int): Boolean
}

class ContractorRepositoryImpl() : ContractorRepository {
    override suspend fun create(contractor: CreateContractorDto): ContractorDto = dbQuery {
        ContractorDao.new {
            name = contractor.name
            workersAmount = contractor.workersAmount
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

    override suspend fun findByEmail(emailWanted: String): ContractorDao? {
        return dbQuery {
            ContractorDao.find { ContractorsTable.email eq emailWanted }.firstOrNull()
        }
    }

    override suspend fun findById(id: Int): ContractorDto? {
        return dbQuery {
            ContractorDao.findById(id)?.toDto()
        }
    }

    override suspend fun update(id: Int, contractor: UpdateContractorDto): ContractorDto? {
        return dbQuery {
            ContractorDao.findByIdAndUpdate(id) { old ->
                contractor.name.let { old.name = it }
                contractor.email.let { old.email = it }
                contractor.workersAmount.let { old.workersAmount = it }
                contractor.rating.let { old.rating = it }
            }?.toDto()
        }
    }

    override suspend fun delete(contractorId: Int): Boolean {
        return dbQuery {
            ContractorsTable.deleteWhere { id.eq(contractorId) }
        } > 0
    }
}