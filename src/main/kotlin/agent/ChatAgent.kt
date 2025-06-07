package agent

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.simpleSingleRunAgent
import ai.koog.agents.local.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import kotlinx.coroutines.runBlocking
import io.github.cdimascio.dotenv.dotenv

suspend fun runAgent(prompt: String): String {
    val switch = Switch()
    val dotenv = dotenv()
    val geminiToken = dotenv["GEMINI_API_KEY"]

    val toolRegistry = ToolRegistry {
        tools(SwitchTools(switch).asTools())
    }

    val agent = simpleSingleRunAgent(
        executor = simpleGoogleAIExecutor(geminiToken),
        llmModel = GoogleModels.Gemini2_0Flash,
        systemPrompt = "You're responsible for running a Switch and perform operations on it by request",
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
    val result = agent.run(prompt)
    return result.toString()
}