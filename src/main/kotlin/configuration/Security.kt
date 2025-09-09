package buildService.configuration

import buildService.configuration.JWTSecurity.jwtAudience
import buildService.configuration.JWTSecurity.jwtIssuer
import buildService.configuration.JWTSecurity.jwtRealm
import buildService.configuration.JWTSecurity.jwtSecret
import buildService.configuration.JWTSecurity.validityInMs
import buildService.features.contactors.ContractorRepository
import buildService.features.users.UserRepository
import buildService.shared.utils.validateEmail
import buildService.shared.utils.validatePassword
import buildService.shared.utils.validateRole
import buildService.shared.utils.verifyPasswords
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.Dotenv
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class LoginDto(val email: String, val password: String, val role: String)

object JWTSecurity {
    lateinit var jwtAudience: String
    lateinit var jwtIssuer: String
    lateinit var jwtRealm: String
    lateinit var jwtSecret: String
    var validityInMs: Long = 3600 * 1000 // 1 час, хотя по идее это вообще не надо.
    // Жалуется, что должно быть обязательно инициализировано
}

fun Application.configureSecurity(dotenv: Dotenv) {
    jwtAudience = dotenv["JWT_AUDIENCE"]
    jwtIssuer = dotenv["JWT_ISSUER"]
    jwtRealm = dotenv["JWT_REALM"]
    jwtSecret = dotenv["JWT_SECRET"]
    validityInMs = dotenv["JWT_VALIDITY_IN_MS"].toLong()

    authentication {
        jwt("jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret)).withAudience(jwtAudience)
                    .withIssuer(jwtIssuer).build()
            )
            validate { credential ->
                // Validate required claims
                val audienceMatches = credential.payload.audience.contains(jwtAudience)
                val emailExists = credential.payload.claims.containsKey("email")

                if (audienceMatches && emailExists) {
                    JWTPrincipal(credential.payload)
                } else {
                    null  // Reject invalid tokens
                }
            }
        }
    }
}

enum class UserRole {
    ADMIN, USER, CONTRACTOR
}

fun Route.authRoutes(userRepository: UserRepository, contractorRepository: ContractorRepository) {
    route("/login") {
        install(RequestValidation) {
            validate<LoginDto> {
                val errors = validateEmail(it.email)
                errors.addAll(validateRole(it.role))
                errors.addAll(validatePassword(it.password))
                if (errors.isEmpty()) ValidationResult.Valid
                else ValidationResult.Invalid(errors)
            }
        }
        post {
            val loginUserDto = call.receive<LoginDto>()
            val role = loginUserDto.role.uppercase()
            val (id, password) = when (role) {
                UserRole.USER.name -> {
                    val user = userRepository.findByEmail(loginUserDto.email)
                        ?: throw NotFoundException("User not found")
                    Pair(user.id.value, user.password)
                }

                UserRole.CONTRACTOR.name -> {
                    val contractor = contractorRepository.findByEmail(loginUserDto.email)
                        ?: throw NotFoundException("Contractor not found")
                    val user = userRepository.findByEmail(loginUserDto.email)
                        ?: throw Exception("user somehow not found")
                    Pair(contractor.id.value, user.password)
                }

                UserRole.ADMIN.name -> {
                    if (loginUserDto.email == "admin@admin" && loginUserDto.password == "admin123") {
                        Pair(-1, "1")
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid password or email")
                        return@post
                    }
                }

                else -> throw BadRequestException("Invalid role")
            }
            if (verifyPasswords(
                    loginUserDto.password, password
                ) || loginUserDto.role == UserRole.ADMIN.name
            ) {
                val token = JWT.create().withAudience(jwtAudience).withIssuer(jwtIssuer)
                    .withClaim("email", loginUserDto.email).withClaim("role", role)
                    .withClaim("id", id.toString())
                    .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
                    .sign(Algorithm.HMAC256(jwtSecret))
                call.respond(LoginResultDto(id, token, role))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid password")
            }

        }
    }
}

@Serializable
data class LoginResultDto(val id: Int, val token: String, val role: String)
