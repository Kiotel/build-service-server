package buildService.features.contactors

import buildService.features.workingSites.WorkingSiteContactorsTable
import buildService.features.workingSites.WorkingSiteDAO
import buildService.features.workingSites.WorkingSiteDto
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.charLength

object ContractorsTable : IntIdTable("contractors") { // Explicit name
    val name = varchar("name", length = 50).check { it.charLength().between(2, 50) }
    val workersAmount = integer("workers_amount").check { it greaterEq 0 }
    val rating = float("rating").check { it lessEq 10f and (it greaterEq 0f) }
}

class ContractorDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContractorDao>(ContractorsTable)

    var name by ContractorsTable.name
    var workersAmount by ContractorsTable.workersAmount.transform({ it }, { it.coerceAtLeast(0) })
    var rating by ContractorsTable.rating.transform({ it }, { it.coerceIn(0f, 10f) })

    val workingSites: SizedIterable<WorkingSiteDAO> by WorkingSiteDAO via WorkingSiteContactorsTable

    fun toDto() = ContractorDto(
        id = this.id.value,
        name = this.name,
        workersAmount = this.workersAmount,
        rating = this.rating,
        workingSites = this.workingSites.map { it.toDto() })
}

@Serializable
data class ContractorDto(
    val id: Int,
    val name: String,
    val workersAmount: Int,
    val rating: Float,
    val workingSites: List<WorkingSiteDto>,
)

@Serializable
data class CreateContractorDto(
    val name: String,
    val workersAmount: Int,
)

@Serializable
data class UpdateContractorDto(
    val name: String?, val workersAmount: Int?, val rating: Float?
)