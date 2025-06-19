package io.github.maikotrindade

import io.github.maikotrindade.agent.executor.agent.TesterAgent
import io.github.maikotrindade.agent.executor.agent.executor.ExecutorInfo
import io.github.maikotrindade.agent.executor.agent.executor.GeminiExecutor
import io.github.maikotrindade.agent.executor.agent.executor.OllamaGwenExecutor
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World Ktor!")
        }

        agentPost("/gemini") { GeminiExecutor() }
        agentPost("/ollama/gwen") { OllamaGwenExecutor() }
    }
}

fun <T> Route.agentPost(path: String, executorProvider: () -> T) where T : Any {
    post(path) {
        val request = call.receive<Map<String, String>>()
        val prompt = request["prompt"] ?: return@post call.respondText("Missing prompt", status = io.ktor.http.HttpStatusCode.BadRequest)
        val result = TesterAgent.runAgent(prompt, executorProvider() as ExecutorInfo)
        call.respondText(result)
    }
}