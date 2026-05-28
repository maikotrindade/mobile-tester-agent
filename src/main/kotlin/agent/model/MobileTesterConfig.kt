package agent.model

import agent.executor.ExecutorInfo
import agent.executor.deepSeek.DeepSeekV4FlashExecutor
import kotlinx.serialization.Serializable

@Serializable
data class MobileTesterConfig(
    var executorInfo: ExecutorInfo = DeepSeekV4FlashExecutor(),
    var llmTemperature: Double = 0.0,
    var maxAgentIterations: Int = 80,
    var logTokensConsumption: Boolean = true,
    var logsEnabled: Boolean = true,
    var screenshotsEnabled: Boolean = true,
    var recordingEnabled: Boolean = false
)