package agent.executor.anthropic

import agent.executor.ExecutorInfo
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import io.github.cdimascio.dotenv.dotenv

class Opus47Executor : ExecutorInfo {
    val dotenv = dotenv()
    override val executor = simpleAnthropicExecutor(dotenv["CLAUDE_API_KEY"])
    override val llmModel = AnthropicModels.Opus_4_7
}