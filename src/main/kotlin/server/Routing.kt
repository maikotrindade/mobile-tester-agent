package server

import agent.ComplexTesterAgent
import agent.TesterAgent
import agent.executor.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.model.AgentRequest

fun Application.configureRouting() {
    routing {
        staticResources("/openapi", "openapi")
        swaggerUI(path = "/swagger", swaggerFile = "openapi/openapi.yaml")
        agentPost("/gemini") { GeminiExecutor() }
        agentPost("/ollama/gwen") { OllamaGwenExecutor() }
        agentPost("/ollama/llama") { OllamaGwenExecutor() }
        agentPost("/openRouter") { OpenRouterExecutor() }
        complexAgentPost("/gemini/complex") { GeminiExecutor() }
        complexAgentPost("/ollama/gwen/complex") { OllamaGwenExecutor() }
        complexAgentPost("/ollama/llama/complex") { OllamaLlamaExecutor() }
        complexAgentPost("/openRouter/complex") { OpenRouterExecutor() }
    }
}

fun <T> Route.agentPost(path: String, executorProvider: () -> T) where T : Any {
    post(path) {
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
            val result = TesterAgent.runAgent(goal, stepsAsStrings, executorProvider() as ExecutorInfo)
            call.respond(result)
        } catch (e: Exception) {
            call.respondText("Error: ${e::class.simpleName}: ${e.message}", status = HttpStatusCode.BadRequest)
        }
    }
}

fun <T> Route.complexAgentPost(path: String, executorProvider: () -> T) where T : Any {
    post(path) {
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
            val result = ComplexTesterAgent.runAgent(goal, stepsAsStrings, executorProvider() as ExecutorInfo)
            call.respond(result)
        } catch (e: Exception) {
            call.respondText("Error: ${e::class.simpleName}: ${e.message}", status = HttpStatusCode.BadRequest)
        }
    }
}