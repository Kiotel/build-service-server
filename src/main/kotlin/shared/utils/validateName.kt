package buildService.shared.utils

fun validateName(name: String?): MutableList<String> {
    val errors = mutableListOf<String>()
    if (name.isNullOrBlank()) {
        errors.add("Name cannot be blank or null")
    } else if (name.length !in 2..50) {
        errors.add("Name length must be from 2 to 50")
    }
    return errors
}