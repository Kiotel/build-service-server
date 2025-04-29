package buildService.shared.utils

import io.ktor.server.auth.jwt.*

fun JWTPrincipal.getInfo(): PrincipalResult {
    val principalId = this.payload.getClaim("id").asString()
    val role = this.payload.getClaim("role").asString()
    val email = this.payload.getClaim("email").asString()
    return PrincipalResult(principalId, role, email)
}

data class PrincipalResult(
    val id: String,
    val role: String,
    val email: String
)