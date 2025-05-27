package buildService.shared.utils

import buildService.configuration.UserRole

fun validateName(name: String?): MutableList<String> {
    val errors = mutableListOf<String>()
    if (name.isNullOrBlank()) {
        errors.add("Name cannot be blank or null")
    } else if (name.length !in 2..50) {
        errors.add("Name length must be from 2 to 50")
    }
    return errors
}

fun validateEmail(email: String?): MutableList<String> {
    val errors = mutableListOf<String>()
    if (email.isNullOrBlank()) {
        errors.add("Email cannot be blank or null")
    } else {
        if (email.length < 3 || !email.contains("@")) {
            errors.add("Email must be valid")
        }
    }
    return errors
}

fun validateRole(role: String?): MutableList<String> {
    val errors = mutableListOf<String>()
    if (role.isNullOrBlank()) {
        errors.add("Role cannot be blank or null")
    } else {
        if (role.uppercase() !in UserRole.entries.map { it.toString() }) {
            errors.add("Role '$role' is not valid")
        }
    }
    return errors
}

fun validatePassword(password: String?): MutableList<String> {
    val errors = mutableListOf<String>()
    if (password.isNullOrBlank()) {
        errors.add("Password cannot be blank or null")
    } else {
        if (password.length < 8) {
            errors.add("Password length must be greater or equal than 8")
        }
    }
    return errors
}

fun validateText(text: String?): MutableList<String> {
    val errors = mutableListOf<String>()
    if (text.isNullOrBlank()) {
        errors.add("Text cannot be blank or null")
    }
    return errors
}