package agent

import agent.executor.GeminiExecutor
import agent.strategy.TestingStrategy
import agent.tool.mobile.test.MobileTestTools
import agent.tool.reporting.ReportingTools
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.feature.handler.AfterLLMCallContext
import ai.koog.agents.core.feature.handler.AgentStartContext
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.params.LLMParams
import kotlinx.coroutines.CompletableDeferred

object MobileTesterAgent {
    suspend fun runAgent(prompt: String): String {
        val resultDeferred = CompletableDeferred<String>()
        val executorInfo = GeminiExecutor()
        val agentConfig = AIAgentConfig(
            prompt = prompt("mobileTester", LLMParams(temperature = 0.0)) {
                system(
                    """
                    You're responsible for testing an Android app and perform tests on it by request.
                    Update the test scenario report every time an action is performed.
                    Close the app and generate a test scenario in the end.
                """.trimIndent()
                )
            },
            model = executorInfo.llmModel,
            maxAgentIterations = 100
        )

        val toolRegistry = ToolRegistry {
            tools(MobileTestTools())
            tools(ReportingTools())
        }

        val agent = AIAgent(
            promptExecutor = executorInfo.executor,
            strategy = TestingStrategy.strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry
        ) {
            handleEvents {
                onBeforeAgentStarted { eventContext: AgentStartContext<*> ->
                    println("Starting strategy: ${eventContext.strategy.name}")
                }

                onToolCall { eventContext ->
                    println(
                        "Tool called: tool ${eventContext.tool.name}, args ${eventContext.toolArgs}"
                    )
                }

                onAgentRunError { eventContext ->
                    println("An error occurred: ${eventContext.throwable.message}\n${eventContext.throwable.stackTraceToString()}")
                }

                onAgentFinished { eventContext ->
                    resultDeferred.complete(
                        if (eventContext.result != null) {
                            "onAgentFinished: ${eventContext.result.toString()}"
                        } else {
                            "Something went wrong."
                        }
                    )
                }

                onAfterLLMCall { eventContext ->
                    logTokensConsumption(eventContext)
                }
            }
        }

        agent.run(prompt)
        return resultDeferred.await()
    }

    private fun logTokensConsumption(eventContext: AfterLLMCallContext) {
        val inputTokensCountList = eventContext.responses.map { it.metaInfo.inputTokensCount }
        var inputTokensCount = 0
        inputTokensCountList.forEach { inputTokensCount += (it ?: 0) }
        println("INPUT tokens count: $inputTokensCount")

        val outputTokensCountList = eventContext.responses.map { it.metaInfo.outputTokensCount }
        var outputTokensCount = 0
        outputTokensCountList.forEach { outputTokensCount += (it ?: 0) }
        println("OUTPUT tokens count: $outputTokensCount")

        val totalTokensCountList = eventContext.responses.map { it.metaInfo.totalTokensCount }
        var totalTokensCount = 0
        totalTokensCountList.forEach { totalTokensCount += (it ?: 0) }
        println("TOTAL tokens count: $totalTokensCount")
    }
}