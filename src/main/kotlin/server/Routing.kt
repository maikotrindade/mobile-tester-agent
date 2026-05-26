package server

import agent.MobileTestAgent
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.model.AgentRequest
import server.model.MobileTesterConfigAPI
import server.model.toMobileConfig

fun Application.configureRouting() {
    routing {
        post("/run-test") {
            try {
                val request = call.receive<AgentRequest>()
                val goal = request.goal
                val packageName = request.packageName
                val stepsAsStrings = request.steps
                if (goal.isBlank()) {
                    return@post call.respondText("Missing goal", status = HttpStatusCode.BadRequest)
                }
                if (packageName.isBlank()) {
                    return@post call.respondText("Missing packageName", status = HttpStatusCode.BadRequest)
                }
                if (stepsAsStrings.isEmpty()) {
                    return@post call.respondText("Missing steps", status = HttpStatusCode.BadRequest)
                }

                println("\n###### API REQUEST\n Goal: $goal \n packageName: $packageName \n steps: $stepsAsStrings \n######\n")
                val result = MobileTestAgent.runAgent(goal, packageName, stepsAsStrings)
                call.respond(result)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText(
                    "Error: ${e::class.simpleName}: ${e.message}",
                    status = HttpStatusCode.InternalServerError
                )
            }
        }

        post("/stop-test") {
            val stopped = MobileTestAgent.stop()
            if (stopped) {
                call.respondText("Test stopped", status = HttpStatusCode.OK)
            } else {
                call.respondText("No test is running", status = HttpStatusCode.Conflict)
            }
        }

        post("/config") {
            try {
                val configApi = call.receive<MobileTesterConfigAPI>()
                MobileTestAgent.updateConfiguration(configApi.toMobileConfig())
                println("Configuration updated: $configApi")
                call.respond(HttpStatusCode.OK, "Configuration updated successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid configuration: ${e.message}")
            }
        }
    }
}
