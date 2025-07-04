package agent

import agent.executor.ExecutorInfo
import agent.tool.MobileTestTools
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import testing.TestScenario

object TesterAgent {
    suspend fun runAgent(goal: String, steps: List<String>, executorInfo: ExecutorInfo): TestScenario {
        val toolRegistry = ToolRegistry.Companion {
            tools(MobileTestTools().asTools())
        }

        val resultDeferred = CompletableDeferred<TestScenario>()

        val agent = AIAgent(
            executor = executorInfo.executor,
            llmModel = executorInfo.llmModel,
            systemPrompt = "You're responsible for testing an Android app and perform tests on it by request." +
                    "You will run Test Scenarios based and will generate a Test Report in the end.",
            temperature = 0.3,
            toolRegistry = toolRegistry,
            maxIterations = 100
        ) {
            install(EventHandler) {
                onToolCall { tool, toolArgs ->
                    println("Tool called: ${tool.name} with args $toolArgs")
                }
                onAgentFinished { strategyName, result ->
                    println("Agent finished with result: $result")
                    // Expecting result to be a JSON string representing TestScenario
                    try {
                        val scenario =
                            Json.decodeFromString<TestScenario>(result ?: throw IllegalArgumentException("No result"))
                        resultDeferred.complete(scenario)
                    } catch (e: Exception) {
                        throw IllegalArgumentException("Failed to parse TestScenario: ${e.message}")
                    }
                }
            }
        }

        val prompt = buildString {
            appendLine("Goal: $goal")
            appendLine("Steps:")
            steps.forEachIndexed { idx, step ->
                appendLine("${idx + 1}. $step")
            }
        }
        agent.run(prompt)
        return resultDeferred.await()
    }
}