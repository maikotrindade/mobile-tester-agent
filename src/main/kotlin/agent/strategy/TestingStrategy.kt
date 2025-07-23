package agent.strategy

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.environment.ReceivedToolResult

object TestingStrategy {
    // Maximum allowed tokens before compressing history
    private const val MAX_TOKENS_THRESHOLD = 1000

    // Defines the agent's workflow strategy for calculator operations
    val strategy = strategy<String, String>("TestingStrategy") {
        // Node: Requests LLM for multiple tool calls
        val nodeCallLLM by nodeLLMRequestMultiple()
        // Node: Executes multiple tools in parallel
        val nodeExecuteToolMultiple by nodeExecuteMultipleTools(parallelTools = true)
        // Node: Sends multiple tool results back to LLM
        val nodeSendToolResultMultiple by nodeLLMSendMultipleToolResults()
        // Node: Compresses history if token usage is high
        val nodeCompressHistory by nodeLLMCompressHistory<List<ReceivedToolResult>>()

        // Start: Forward to LLM to determine next action
        edge(nodeStart forwardTo nodeCallLLM)

        // If LLM returns an assistant message, finish with the first result
        edge(
            (nodeCallLLM forwardTo nodeFinish)
                    transformed { it.first() }
                    onAssistantMessage { true }
        )

        // If LLM requests multiple tool calls, execute them in parallel
        edge(
            (nodeCallLLM forwardTo nodeExecuteToolMultiple)
                    onMultipleToolCalls { true }
        )

        // If token usage exceeds threshold, compress history before sending results
        edge(
            (nodeExecuteToolMultiple forwardTo nodeCompressHistory)
                    onCondition { llm.readSession { prompt.latestTokenUsage > MAX_TOKENS_THRESHOLD } }
        )

        // After compressing history, send tool results to LLM
        edge(nodeCompressHistory forwardTo nodeSendToolResultMultiple)

        // If token usage is within threshold, send tool results directly to LLM
        edge(
            (nodeExecuteToolMultiple forwardTo nodeSendToolResultMultiple)
                    onCondition { llm.readSession { prompt.latestTokenUsage <= MAX_TOKENS_THRESHOLD } }
        )

        // If LLM requests more tool calls after receiving results, execute again
        edge(
            (nodeSendToolResultMultiple forwardTo nodeExecuteToolMultiple)
                    onMultipleToolCalls { true }
        )

        // If LLM returns an assistant message after tool results, finish with the first result
        edge(
            (nodeSendToolResultMultiple forwardTo nodeFinish)
                    transformed { it.first() }
                    onAssistantMessage { true }
        )
    }
}