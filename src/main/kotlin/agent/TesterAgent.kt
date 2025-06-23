package agent

import agent.executor.ExecutorInfo
import agent.tool.MobileTestTools
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import kotlinx.coroutines.CompletableDeferred

object TesterAgent {
    suspend fun runAgent(prompt: String, executorInfo: ExecutorInfo): String {
        val toolRegistry = ToolRegistry.Companion {
            tools(MobileTestTools().asTools())
        }

        val resultDeferred = CompletableDeferred<String>()

        val agent = AIAgent(
            executor = executorInfo.executor,
            llmModel = executorInfo.llmModel,
            systemPrompt = "You're responsible for testing an Android app and perform operations on it by request",
            temperature = 0.2,
            toolRegistry = toolRegistry,
            maxIterations = 100
        ) {
            install(EventHandler) {
                onToolCall { tool, toolArgs ->
                    println("Tool called: ${tool.name} with args $toolArgs")
                }
                onAgentFinished { strategyName, result ->
                    println("Agent finished with result: $result")
                    resultDeferred.complete(result ?: "Error: no result")
                }
            }
        }

        agent.run(prompt)
        return resultDeferred.await()
    }
}