package buildService.features.users

import buildService.shared.utils.dbQuery
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface UserRepository {
    suspend fun create(user: CreateUserDto): UserDto
    suspend fun findAll(): List<UserDto>
    suspend fun findById(id: Int): UserDto?
    suspend fun update(id: Int, userDto: UpdateUserDto)
    suspend fun delete(id: Int)
}

class UserRepositoryImpl() : UserRepository {
    override suspend fun create(user: CreateUserDto): UserDto = dbQuery {
        UserDao.new {
            name = user.name
            age = user.age
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

    override suspend fun update(id: Int, user: UpdateUserDto) {
        dbQuery {
            UserDao.findByIdAndUpdate(id) {
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