package buildService.shared.utils

import at.favre.lib.crypto.bcrypt.BCrypt

fun String.hashPassword(): String {
    return BCrypt.withDefaults().hashToString(12, this.toCharArray()).toString()
}

fun verifyPasswords(password: String, hashedPassword: String): Boolean {
    val result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword.toCharArray())
    return result.verified
}