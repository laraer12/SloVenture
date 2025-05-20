package api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientProvider {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                classDiscriminator = "type"
                useArrayPolymorphism = false
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis  = 10_000
        }
    }
}