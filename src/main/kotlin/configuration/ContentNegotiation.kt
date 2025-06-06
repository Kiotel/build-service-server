package buildService.configuration

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureContentNegotiation() {
    val json = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
        explicitNulls = false
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    install(ContentNegotiation) {
        json(json)
    }
}
