package buildService.features.users

import buildService.shared.utils.dbQuery
import buildService.shared.utils.hashPassword
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

interface UserRepository {
    suspend fun create(user: RegisterUserDto): UserDto
    suspend fun findByEmail(email: String): UserDao?
    suspend fun findAll(): List<UserDto>
    suspend fun findById(userId: Int): UserDto?
    suspend fun update(userId: Int, updateUserDto: UpdateUserDto): UserDto?
    suspend fun delete(userId: Int): Boolean
}

class UserRepositoryImpl() : UserRepository {
    override suspend fun create(user: RegisterUserDto): UserDto = dbQuery {
        val passwordHashed = user.password.hashPassword()
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

    override suspend fun findById(userId: Int): UserDto? {
        return dbQuery {
            UserDao.findById(userId)?.toDto()
        }
    }

    override suspend fun findByEmail(email: String): UserDao? {
        return dbQuery {
            UserDao.find { UsersTable.email eq email }.firstOrNull()
        }
    }

    override suspend fun update(userId: Int, updateUserDto: UpdateUserDto): UserDto? {
        return dbQuery {
            val user = UserDao.findById(userId)
            user?.apply {
                updateUserDto.name.let { name = it }
                updateUserDto.email.let { email = it }
                updatedAt = Clock.System.now()
            }
            user?.toDto()
        }
    }

    override suspend fun delete(userId: Int): Boolean {
        return dbQuery {
            UsersTable.deleteWhere { id.eq(userId) }
        } > 0
    }
}