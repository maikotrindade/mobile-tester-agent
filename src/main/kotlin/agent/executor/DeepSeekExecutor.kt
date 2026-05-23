package agent.executor

import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import io.github.cdimascio.dotenv.dotenv
import kotlin.time.ExperimentalTime

class DeepSeekExecutor : ExecutorInfo {
    val dotenv = dotenv()

    @OptIn(ExperimentalTime::class)
    override val executor = MultiLLMPromptExecutor(DeepSeekLLMClient(dotenv["DEEP_SEEK_KEY"]))
    override val llmModel = DeepSeekModels.DeepSeekChat
}