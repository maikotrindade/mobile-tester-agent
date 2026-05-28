package server

import agent.MobileTestAgent
import agent.reporting.ReportRecorder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.model.AgentRequest
import server.model.MobileTesterConfigAPI
import server.model.toMobileConfig
import java.io.File

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

        get("/report") {
            val manifest = File(ReportRecorder.rootDir, "manifest.json")
            if (!manifest.exists()) {
                return@get call.respondText("No report available", status = HttpStatusCode.NotFound)
            }
            call.respondText(manifest.readText(), ContentType.Application.Json, HttpStatusCode.OK)
        }

        get("/report/file") {
            val rel = call.request.queryParameters["path"]
            if (rel.isNullOrBlank()) {
                return@get call.respondText("Missing path", status = HttpStatusCode.BadRequest)
            }
            val root = ReportRecorder.rootDir.canonicalFile
            val target = File(root, rel).canonicalFile
            if (!target.path.startsWith(root.path + File.separator) && target.path != root.path) {
                return@get call.respondText("Forbidden", status = HttpStatusCode.Forbidden)
            }
            if (!target.exists() || !target.isFile) {
                return@get call.respondText("Not found", status = HttpStatusCode.NotFound)
            }
            val contentType = when (target.extension.lowercase()) {
                "png" -> ContentType.Image.PNG
                "jpg", "jpeg" -> ContentType.Image.JPEG
                "mp4" -> ContentType.Video.MP4
                "json" -> ContentType.Application.Json
                "txt", "log" -> ContentType.Text.Plain
                else -> ContentType.Application.OctetStream
            }
            call.respondBytes(target.readBytes(), contentType)
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
