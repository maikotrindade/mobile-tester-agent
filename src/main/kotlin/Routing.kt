package io.github.maikotrindade

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World Ktor!")
        }
        post("/run-agent") {
            val request = call.receive<Map<String, String>>()
            val prompt = request["prompt"] ?: return@post call.respondText("Missing prompt", status = io.ktor.http.HttpStatusCode.BadRequest)
            val result = agent.runAgent(prompt)
            call.respondText(result)
        }
        post("/run-tester") {
            val request = call.receive<Map<String, String>>()
            val prompt = request["prompt"] ?: return@post call.respondText("Missing prompt", status = io.ktor.http.HttpStatusCode.BadRequest)
            val result = agent.runMobileTestAgent(prompt)
            call.respondText(result)
        }
    }
}
