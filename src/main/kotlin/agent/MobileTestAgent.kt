package agent

import agent.tool.MobileTestTools
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.simpleSingleRunAgent
import ai.koog.agents.local.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CompletableDeferred

suspend fun runMobileTestAgent(prompt: String): String {
    val dotenv = dotenv()
    val geminiToken = dotenv["GEMINI_API_KEY"]

    val toolRegistry = ToolRegistry {
        tools(MobileTestTools().asTools())
    }

    val resultDeferred = CompletableDeferred<String>()

    val agent = simpleSingleRunAgent(
        executor = simpleGoogleAIExecutor(geminiToken),
        llmModel = GoogleModels.Gemini2_0Flash,
        systemPrompt = "You are a mobile app testing agent. Use the provided tools to interact with the Android emulator and test the app as instructed.",
        temperature = 0.0,
        toolRegistry = toolRegistry
    ) {
        handleEvents {
            onToolCall = { tool, toolArgs ->
                println("Tool called: ${tool.name} with args $toolArgs")
            }

            onAgentFinished = { strategyName, result ->
                println("Agent finished with result: $result")
            }
        }
    }

    agent.run(prompt)
    return resultDeferred.await()
}
