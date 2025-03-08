package buildService.features.workingSites

import buildService.features.users.UserDao
import buildService.features.users.UserDto
import buildService.features.users.UsersTable
import buildService.features.users.UsersTable.age
import buildService.shared.utils.dbQuery
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface UserRepository{
    suspend fun findAll(): List<UserDto>
    suspend fun findById(id: Int): UserDto?
    suspend fun update(userDto: UserDto)
    suspend fun delete(id: Int)
}

class UserRepositoryImpl() : UserRepository {
    suspend fun create(user: UserDto) = dbQuery {
        UserDao.new {
            name = user.name
            age = user.age
        }
    }

    override suspend fun findAll(): List<UserDto> {
        return dbQuery {
            UserDao.all().map(UserDao::toDto)
        }
    }

    override suspend fun findById(id: Int): UserDto? {
        return dbQuery {
            UserDao.findById(id)?.toDto()
        }
    }

    override suspend fun update(user: UserDto) {
        dbQuery {
            UserDao.findByIdAndUpdate(user.id) {
                it.name = user.name
                it.age = user.age
            }
        }
    }

    override suspend fun delete(id: Int) {
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        }
    }
}