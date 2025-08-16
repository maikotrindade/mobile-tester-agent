package agent.model

import agent.executor.ExecutorInfo
import agent.executor.GeminiExecutor
import kotlinx.serialization.Serializable

@Serializable
data class MobileTesterConfig(
    var executorInfo: ExecutorInfo = GeminiExecutor(),
    var llmTemperature: Double = 0.0,
    var maxAgentIterations: Int = 50,
    var logTokensConsumption: Boolean = false
)