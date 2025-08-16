package server

import agent.MobileTestAgent
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.model.AgentRequest
import server.model.MobileTesterConfigAPI
import server.model.toMobileConfig

fun Application.configureRouting() {
    routing {
        staticResources("/openapi", "openapi")
        swaggerUI(path = "/swagger", swaggerFile = "openapi/openapi.yaml")

        post("/run-test") {
            try {
                val request = call.receive<AgentRequest>()
                val goal = request.goal
                val stepsAsStrings = request.steps
                if (goal.isBlank()) {
                    return@post call.respondText(
                        "Missing goal",
                        status = HttpStatusCode.BadRequest
                    )
                }
                if (stepsAsStrings.isEmpty()) {
                    return@post call.respondText(
                        "Missing steps",
                        status = HttpStatusCode.BadRequest
                    )
                }

                println("\n###### API REQUEST\n Goal: $goal \n steps: $stepsAsStrings \n######\n")
                val result = MobileTestAgent.runAgent(goal, stepsAsStrings)
                call.respond(result)
            } catch (e: Exception) {
                call.respondText(
                    "Error: ${e::class.simpleName}: ${e.message}",
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        post("/config") {
            try {
                val configApi = call.receive<MobileTesterConfigAPI>()
                MobileTestAgent.updateConfiguration(configApi.toMobileConfig())
                call.respond(HttpStatusCode.OK, "Configuration updated successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid configuration: ${e.message}")
            }
        }
    }
}
