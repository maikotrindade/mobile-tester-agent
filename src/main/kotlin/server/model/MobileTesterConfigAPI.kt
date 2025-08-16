package server.model

import agent.executor.GeminiExecutor
import agent.executor.OllamaGwenExecutor
import agent.executor.OllamaLlamaExecutor
import agent.executor.OpenRouterExecutor
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
        "gemini" -> GeminiExecutor()
        "ollama_gwen" -> OllamaGwenExecutor()
        "ollama_llama" -> OllamaLlamaExecutor()
        "open_router" -> OpenRouterExecutor()
        else -> throw IllegalArgumentException("Unknown executorInfoId: $executorInfoId")
    },
    llmTemperature = llmTemperature,
    maxAgentIterations = maxAgentIterations,
    logTokensConsumption = logTokensConsumption
)
