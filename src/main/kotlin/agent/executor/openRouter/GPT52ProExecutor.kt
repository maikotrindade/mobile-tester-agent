package agent.executor.openRouter

import agent.executor.ExecutorInfo
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import io.github.cdimascio.dotenv.dotenv

class GPT52ProExecutor : ExecutorInfo {
    val dotenv = dotenv()
    override val executor = simpleOpenRouterExecutor(dotenv["OPEN_ROUTER"])
    override val llmModel = OpenRouterModels.GPT5_2Pro
}