package agent.executor

import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import io.github.cdimascio.dotenv.dotenv

class OpenRouterExecutor : ExecutorInfo {
    val dotenv = dotenv()
    override val executor = simpleOpenRouterExecutor(dotenv["OPEN_ROUTER"])
    override val llmModel = OpenRouterModels.GPT35Turbo
}