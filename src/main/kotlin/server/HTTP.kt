package server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import testing.TestResult

fun Application.configureHTTP() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                serializersModule = SerializersModule {
                    polymorphic(TestResult::class) {
                        subclass(TestResult.Passed::class)
                        subclass(TestResult.Failed::class)
                        subclass(TestResult.Pending::class)
                        subclass(TestResult.Skipped::class)
                        subclass(TestResult.Error::class)
                    }
                }
            }
        )
    }
    routing {
        swaggerUI(path = "openapi")
    }
}
