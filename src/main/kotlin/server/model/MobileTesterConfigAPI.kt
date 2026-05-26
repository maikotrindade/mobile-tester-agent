package server.model

import agent.executor.anthropic.Opus47Executor
import agent.executor.deepSeek.DeepSeekV4FlashExecutor
import agent.executor.google.Gemini3ProExecutor
import agent.executor.ollama.Grok8BExecutor
import agent.executor.ollama.Llama4Executor
import agent.executor.ollama.QWEN36BExecutor
import agent.executor.openRouter.GPT52ProExecutor
import agent.model.MobileTesterConfig
import kotlinx.serialization.Serializable

@Serializable
data class MobileTesterConfigAPI(
    var executorInfoId: String = "gemini",
    var llmTemperature: Double = 0.0,
    var maxAgentIterations: Int = 50,
    var logTokensConsumption: Boolean = false
)

fun MobileTesterConfigAPI.toMobileConfig() = MobileTesterConfig(
    executorInfo = when (executorInfoId.lowercase()) {
        "Opus47" -> Opus47Executor()
        "DeepSeekV4Flash" -> DeepSeekV4FlashExecutor()
        "Gemini3Pro" -> Gemini3ProExecutor()
        "QWEN36B" -> QWEN36BExecutor()
        "Llama4" -> Llama4Executor()
        "GPT52Pro" -> GPT52ProExecutor()
        "Grok8BExecutor" -> Grok8BExecutor()
        else -> throw IllegalArgumentException("Unknown executorInfoId: $executorInfoId")
    },
    llmTemperature = llmTemperature,
    maxAgentIterations = maxAgentIterations,
    logTokensConsumption = logTokensConsumption
)
