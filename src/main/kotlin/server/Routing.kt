package server

import agent.MobileTestAgent
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
        agentRunPost("/gemini_2_0Flash") { GeminiExecutor() }
        agentRunPost("/ollama/gwen_3_06B") { OllamaGwenExecutor() }
        agentRunPost("/ollama/llama_3_2_3B") { OllamaLlamaExecutor() }
        agentRunPost("/openRouter/gpt_4") { OpenRouterExecutor() }
    }
}

fun <T> Route.agentRunPost(path: String, executorProvider: () -> T) where T : Any {
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

            println("\n###### API REQUEST\n Goal: $goal \n steps: $stepsAsStrings \n######\n")

            val result = MobileTestAgent.runAgent(goal, stepsAsStrings, executorProvider() as ExecutorInfo)
            call.respond(result)
        } catch (e: Exception) {
            call.respondText("Error: ${e::class.simpleName}: ${e.message}", status = HttpStatusCode.InternalServerError)
        }
    }
}