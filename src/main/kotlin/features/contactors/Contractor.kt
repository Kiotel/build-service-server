package buildService.features.contactors

import buildService.features.workingSites.WorkingSiteContractorsTable
import buildService.features.workingSites.WorkingSiteDao
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.charLength

object ContractorsTable : IntIdTable("contractors") {
    val name = varchar("name", 50).check { it.charLength().between(2, 50) }
    val email = varchar("email", length = 255).uniqueIndex()
    var password = varchar("password", length = 255).uniqueIndex()
    val workersAmount = integer("workers_amount").check { it greaterEq 0 }
    val rating = float("rating").check { it.between(0f, 10f) }
}

class ContractorDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContractorDao>(ContractorsTable)

    var name by ContractorsTable.name
    var email by ContractorsTable.email
    var workersAmount by ContractorsTable.workersAmount.transform({ it }, { it.coerceAtLeast(0) })
    var rating by ContractorsTable.rating.transform({ it }, { it.coerceIn(0f, 10f) })
    var password by ContractorsTable.password

    var workingSites by WorkingSiteDao via WorkingSiteContractorsTable

    fun toDto() = ContractorDto(
        id = this.id.value,
        name = this.name,
        email = this.email,
        workersAmount = this.workersAmount,
        rating = this.rating,
        workingSitesIds = this.workingSites.map { it.id.value }.toList()
    )
}

@Serializable
data class ContractorDto(
    val id: Int,
    val name: String,
    val email: String,
    val workersAmount: Int,
    val rating: Float,
    val workingSitesIds: List<Int>
)

@Serializable
data class RegisterContractorDto(
    val name: String,
    val email: String,
    val workersAmount: Int,
    val password: String,
    val workingSitesIds: List<Int>?
)

@Serializable
data class UpdateContractorDto(
    val name: String, val email: String, val workersAmount: Int, val rating: Float
)