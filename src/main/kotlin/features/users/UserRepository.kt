package buildService.features.users

import buildService.shared.utils.dbQuery
import buildService.shared.utils.hashPassword
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface UserRepository {
    suspend fun create(user: RegisterUserDto): UserDto
    suspend fun findByEmail(email: String): UserDao?
    suspend fun findAll(): List<UserDto>
    suspend fun findById(id: Int): UserDto?
    suspend fun update(id: Int, userDto: UpdateUserDto)
    suspend fun delete(id: Int): Boolean
}

class UserRepositoryImpl() : UserRepository {
    override suspend fun create(user: RegisterUserDto): UserDto = dbQuery {
        var passwordHashed = user.password.hashPassword()
        UserDao.new {
            name = user.name
            email = user.email
            password = passwordHashed
        }.toDto()
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

    override suspend fun findByEmail(emailWanted: String): UserDao? {
        return dbQuery {
            UserDao.find { UsersTable.email eq emailWanted }.firstOrNull()
        }
    }

    override suspend fun update(id: Int, user: UpdateUserDto) {
        dbQuery {
            UserDao.findByIdAndUpdate(id) {
                it.name = user.name
                it.email = user.email
            }
        }
    }

    override suspend fun delete(id: Int): Boolean {
        return dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
        } > 0
    }
}