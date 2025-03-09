package buildService.features.contactors

import buildService.features.contactors.ContractorsTable.rating
import buildService.features.contactors.ContractorsTable.workersAmount
import buildService.features.users.UsersTable
import buildService.features.workingSites.WorkingSiteDao
import buildService.features.workingSites.WorkingSiteDto
import buildService.features.workingSites.WorkingSitesTable
import buildService.shared.utils.dbQuery
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.sql.EmptySizedIterable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
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
    override suspend fun create(contractorDto: CreateContractorDto): ContractorDto = dbQuery {
        val workingSites = contractorDto.workingSitesIds?.let { ids ->
            WorkingSiteDao.find { WorkingSitesTable.id inList ids }.toList().also {
                if (it.size != ids.size) throw NotFoundException("Some working sites not found")
            }
        } ?: emptyList()

        ContractorDao.new {
            name = contractorDto.name
            workersAmount = contractorDto.workersAmount
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

    override suspend fun findById(id: Int): ContractorDto? {
        return dbQuery {
            ContractorDao.findById(id)?.toDto()
        }
    }

    override suspend fun update(id: Int, contractor: UpdateContractorDto) {
        dbQuery {
            ContractorDao.findByIdAndUpdate(id) { old ->
                contractor.name?.let { old.name = it }
                contractor.workersAmount?.let { old.workersAmount = it }
                contractor.rating?.let { old.rating = it }
            }
        }
    }

    override suspend fun delete(id: Int) {
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        }
    }
}