@file:OptIn(ExperimentalSerializationApi::class)

package features.users

import buildService.configuration.LoginDto
import buildService.configuration.LoginResultDto
import buildService.configuration.UserRole
import buildService.features.users.RegisterUserDto
import buildService.features.users.UserDto
import buildService.module
import createTestClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UserRoutesTest {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(UserRoutesTest::class.java)
        val testConfig = ApplicationConfig("testApplication.yaml")
        var adminToken: String = ""
        var userToken: String = ""
        var userId: Int = -1
        var users: List<UserDto> = emptyList()
    }

    @Test
    fun test_0_adminLogin() = testApplication {
        application { module(testConfig) }
        val client = createTestClient()
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginDto(
                    email = "admin@admin", password = "admin123",
                    role = "admin"
                )
            )
        }
        assertEquals(HttpStatusCode.OK, response.status, "Admin login failed")
        val result: LoginResultDto = response.body()
        assertNotEquals("", result.token, "Admin token is empty")
        assertEquals(UserRole.ADMIN.name, result.role, "Admin role isn't admin")
        adminToken = result.token
        logger.info("Logged as admin with token $adminToken")
    }

    @Test
    fun test_1_getAllUsers() = testApplication {
        application { module(testConfig) }
        val client = createTestClient()
        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status, "Couldn't get all users")
        users = response.body()
        logger.info("All users found: $users")
    }

    @Test
    fun test_2_deleteAllUsers() = testApplication {
        application { module(testConfig) }
        val client = createTestClient()
        assertNotEquals("", adminToken, "Admin token is not set")
        users.forEach { user ->
            val response = client.delete("/users/${user.id}") {
                bearerAuth(adminToken)
            }
            assertEquals(
                HttpStatusCode.NoContent, response.status, "Couldn't delete user with ID ${user.id}"
            )
            logger.info("Deleted user with id ${user.id}")
        }
    }

    @Test
    fun test_3_registerUser() = testApplication {
        application { module(testConfig) }
        val client = createTestClient()
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(RegisterUserDto(name = "test", email = "test@test.test", password = "testtest"))
        }
        assertEquals(HttpStatusCode.Created, response.status, "Failed to create user")
        val newUser: UserDto = response.body()
        assertEquals("test", newUser.name)
        assertEquals("test@test.test", newUser.email)
        userId = newUser.id
        logger.info("Successfully registered user: $newUser")
    }

    @Test
    fun test_4_userLogin() = testApplication {
        application { module(testConfig) }
        val client = createTestClient()
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginDto(
                    email = "test@test.test", password = "testtest",
                    role = "user"
                )
            )
        }
        assertEquals(HttpStatusCode.OK, response.status, "User login failed")
        val result: LoginResultDto = response.body()
        assertNotEquals(-1, result.id, "Couldn't get user id")
        assertNotEquals("", result.token, "User token is empty")
        assertEquals(UserRole.USER.name, result.role, "User role isn't user")
        userToken = result.token
        logger.info("Successfully logged in as user with token $userToken")
    }

    @Test
    fun test_5_deleteUser() = testApplication {
        application { module(testConfig) }
        val client = createTestClient()
        assertNotEquals(-1, userId, "User ID is not set")
        assertNotEquals("", userToken, "User token is not set")
        val response = client.delete("/users/$userId") {
            bearerAuth(userToken)
        }
        assertEquals(
            HttpStatusCode.NoContent,
            response.status,
            "Couldn't delete user with ID $userId"
        )
        logger.info("Successfully deleted user with ID $userId")
    }
}