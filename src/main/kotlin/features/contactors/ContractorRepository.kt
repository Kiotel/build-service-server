package buildService.features.contactors

import buildService.features.users.UsersTable
import buildService.features.workingSites.WorkingSiteDao
import buildService.features.workingSites.WorkingSitesTable
import buildService.shared.utils.dbQuery
import buildService.shared.utils.hashPassword
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface ContractorRepository {
    suspend fun create(user: RegisterContractorDto): ContractorDto
    suspend fun findAll(): List<ContractorDto>
    suspend fun findById(id: Int): ContractorDto?
    suspend fun findByEmail(email: String): ContractorDao?
    suspend fun update(id: Int, contractorDto: UpdateContractorDto)
    suspend fun delete(id: Int)
}

class ContractorRepositoryImpl() : ContractorRepository {
    override suspend fun create(contractor: RegisterContractorDto): ContractorDto = dbQuery {
        val workingSites = contractor.workingSitesIds?.let { ids ->
            WorkingSiteDao.find { WorkingSitesTable.id inList ids }.toList().also {
                if (it.size != ids.size) throw NotFoundException("Some working sites not found")
            }
        } ?: emptyList()

        ContractorDao.new {
            name = contractor.name
            workersAmount = contractor.workersAmount
            email = contractor.email
            password = contractor.password.hashPassword()
            rating = 7f
        }.apply {
            this.workingSites = SizedCollection(workingSites)
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

    override suspend fun update(id: Int, contractor: UpdateContractorDto) {
        dbQuery {
            ContractorDao.findByIdAndUpdate(id) { old ->
                contractor.name.let { old.name = it }
                contractor.email.let { old.email = it }
                contractor.workersAmount.let { old.workersAmount = it }
                contractor.rating.let { old.rating = it }
            }
        }
    }

    override suspend fun delete(id: Int) {
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        }
    }
}