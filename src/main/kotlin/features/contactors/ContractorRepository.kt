package buildService.features.contactors

import buildService.features.users.UsersTable
import buildService.features.workingSites.WorkingSiteContactorsTable.contractor
import buildService.shared.utils.dbQuery
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface ContractorRepository {
    suspend fun create(user: CreateContractorDto): ContractorDto
    suspend fun findAll(): List<ContractorDto>
    suspend fun findById(id: Int): ContractorDto?
    suspend fun update(id: Int, contractorDto: UpdateContractorDto)
    suspend fun delete(id: Int)
}

class ContractorRepositoryImpl() : ContractorRepository {
    override suspend fun create(contractor: CreateContractorDto) = dbQuery {
        ContractorDao.new {
            name = contractor.name
            workersAmount = contractor.workersAmount
            rating = 7f
        }.toDto()
    }

    override suspend fun findAll(): List<ContractorDto> {
        return dbQuery {
            ContractorDao.all().map(ContractorDao::toDto)
        }
    }

    override suspend fun findById(id: Int): ContractorDto? {
        return dbQuery {
            ContractorDao.findById(id)?.toDto()
        }
    }

    override suspend fun update(id: Int, contractor: UpdateContractorDto) {
        dbQuery {
            ContractorDao.findByIdAndUpdate(id) { old ->
                contractor.name?.let {old.name = it  }
                contractor.workersAmount?.let {old.workersAmount = it }
                contractor.rating?.let {old.rating = it }
            }
        }
    }

    override suspend fun delete(id: Int) {
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        }
    }
}