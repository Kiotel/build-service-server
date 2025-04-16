package buildService.features.users

import buildService.features.contactors.ContractorDao
import buildService.features.contactors.ContractorsTable
import buildService.features.workingSites.CreateWorkingSiteDto
import buildService.features.workingSites.UpdateWorkingSiteDto
import buildService.features.workingSites.WorkingSiteDao
import buildService.features.workingSites.WorkingSiteDto
import buildService.shared.utils.dbQuery
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface WorkingSiteRepository {
    suspend fun create(user: CreateWorkingSiteDto): WorkingSiteDto
    suspend fun findAll(): List<WorkingSiteDto>
    suspend fun findById(id: Int): WorkingSiteDto?
    suspend fun update(id: Int, workingSiteDto: UpdateWorkingSiteDto)
    suspend fun delete(id: Int)
}

class WorkingSiteRepositoryImpl() : WorkingSiteRepository {
    override suspend fun create(workingSite: CreateWorkingSiteDto): WorkingSiteDto = dbQuery {
        val user = UserDao.findById(workingSite.userId)
            ?: throw NotFoundException("User with id ${workingSite.userId} not found")

        WorkingSiteDao.new {
            name = workingSite.name
            this.user = user
        }.toDto()
    }

    override suspend fun findAll(): List<WorkingSiteDto> {
        return dbQuery {
            WorkingSiteDao.all().map(WorkingSiteDao::toDto)
        }
    }

    override suspend fun findById(id: Int): WorkingSiteDto? {
        return dbQuery {
            WorkingSiteDao.findById(id)?.toDto()
        }
    }

    override suspend fun update(id: Int, updateDto: UpdateWorkingSiteDto) {
        dbQuery {
            val workingSite = WorkingSiteDao.findById(id)
                ?: throw NotFoundException("Working site with id $id not found")

            updateDto.name?.let { workingSite.name = it }

            updateDto.contractorsIds?.let { ids ->
                val contractors = ContractorDao.find { ContractorsTable.id inList ids }.toList()
                if (contractors.size != ids.size) throw NotFoundException()
                workingSite.contractors = SizedCollection(contractors)
            }
        }
    }

    override suspend fun delete(id: Int) {
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        }
    }
}