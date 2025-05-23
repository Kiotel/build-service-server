package buildService.features.workingSites

import buildService.features.contactors.ContractorDao
import buildService.features.contactors.ContractorsTable
import buildService.features.users.UserDao
import buildService.features.users.UsersTable
import buildService.shared.utils.dbQuery
import io.ktor.server.plugins.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface WorkingSiteRepository {
    suspend fun create(workingSite: CreateWorkingSiteDto): WorkingSiteDto
    suspend fun findAll(): List<WorkingSiteDto>
    suspend fun findById(id: Int): WorkingSiteDto?
    suspend fun update(id: Int, updateDto: UpdateWorkingSiteDto): WorkingSiteDto
    suspend fun delete(id: Int): Boolean
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

    override suspend fun update(id: Int, updateDto: UpdateWorkingSiteDto): WorkingSiteDto {
        return dbQuery {
            val workingSite = WorkingSiteDao.findById(id)
                ?: throw NotFoundException("Working site with id $id not found")

            updateDto.name?.let { workingSite.name = it }

            updateDto.contractorsIds?.let { desiredIds ->
                if (desiredIds.isNotEmpty()) {
                    val foundContractors =
                        ContractorDao.find { ContractorsTable.id inList desiredIds }.toList()
                    val foundContractorIds = foundContractors.map { it.id.value }.toSet()

                    if (foundContractors.size != desiredIds.size) {
                        val missingIds = desiredIds.filterNot { it in foundContractorIds }
                        throw NotFoundException(
                            "The following contractor IDs were not found: ${
                                missingIds.joinToString(
                                    ", "
                                )
                            }"
                        )
                    }
                    workingSite.contractors = SizedCollection(foundContractors)
                } else {
                    workingSite.contractors = SizedCollection(emptyList())
                }
            }

            workingSite.updatedAt = Clock.System.now()
            return@dbQuery workingSite.toDto()
        }
    }

    override suspend fun delete(id: Int): Boolean {
        return dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        } > 0
    }
}