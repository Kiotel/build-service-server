package buildService.shared.utils

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.server.engine.*

fun String.hashPassword(): String {
    return BCrypt.withDefaults().hashToString(12, this.toCharArray()).toString()
}

fun verifyPasswords(password1: String, password2: String): Boolean {
    val result = BCrypt.verifyer().verify(password1.toCharArray(), password2.toCharArray())
    applicationEnvironment().log.info(result.toString())
    return result.verified
}