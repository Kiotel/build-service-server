@file:OptIn(ExperimentalSerializationApi::class)

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

fun HttpClientConfig<out HttpClientEngineConfig>.configureJson() {
    install(ContentNegotiation) {
        json(Json {
            namingStrategy = JsonNamingStrategy.SnakeCase
            explicitNulls = false
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun ApplicationTestBuilder.createTestClient(): HttpClient {
    return createClient { configureJson() }
}