package buildService.schemas

import buildService.schemas.UserService.Users.age
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Contactor(val name: String, val workersAmount: Int, val rating: Float = 7f)

class ContactorService(database: Database) {
    object Contactors : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val workersAmount = integer("amout")
        val rating = float("rating").check { it greaterEq 0f and (it lessEq 10f) }

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Contactors)
        }
    }

    suspend fun create(contactor: Contactor): Int = dbQuery {
        Contactors.insert {
            it[name] = contactor.name
            it[workersAmount] = contactor.workersAmount
        }[Contactors.id]
    }

    suspend fun read(id: Int): Contactor? {
        return dbQuery {
            Contactors.selectAll()
                .where { Contactors.id eq id }
                .map { Contactor(it[Contactors.name], it[Contactors.workersAmount], it[Contactors.rating]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Contactors.update({ Contactors.id eq id }) {
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Contactors.deleteWhere { Contactors.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

