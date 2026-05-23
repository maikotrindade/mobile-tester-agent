package agent.strategy

import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.ReceivedToolResults
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMCompressHistory
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResults
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls

object TestingStrategy {
    private const val MAX_TOKENS_THRESHOLD = 8000

    val strategy = strategy<String, String>("TestingStrategy") {
        val nodeCallLLM by nodeLLMRequest()
        val nodeExecuteToolMultiple by nodeExecuteTools(parallel = true)
        val nodeSendToolResultMultiple by nodeLLMSendToolResults()
        val nodeCompressHistory by nodeLLMCompressHistory<ReceivedToolResults>()

        edge(nodeStart forwardTo nodeCallLLM)

        edge(
            (nodeCallLLM forwardTo nodeFinish)
                    onTextMessage { true }
        )

        edge(
            (nodeCallLLM forwardTo nodeExecuteToolMultiple)
                    onToolCalls { true }
        )

        edge(
            (nodeExecuteToolMultiple forwardTo nodeCompressHistory)
                    onCondition { llm.readSession { prompt.latestTokenUsage > MAX_TOKENS_THRESHOLD } }
        )

        edge(nodeCompressHistory forwardTo nodeSendToolResultMultiple)

        edge(
            (nodeExecuteToolMultiple forwardTo nodeSendToolResultMultiple)
                    onCondition { llm.readSession { prompt.latestTokenUsage <= MAX_TOKENS_THRESHOLD } }
        )

        edge(
            (nodeSendToolResultMultiple forwardTo nodeExecuteToolMultiple)
                    onToolCalls { true }
        )

        edge(
            (nodeSendToolResultMultiple forwardTo nodeFinish)
                    onTextMessage { true }
        )
    }
}
