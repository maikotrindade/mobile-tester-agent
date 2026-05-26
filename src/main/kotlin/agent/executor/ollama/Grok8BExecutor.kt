package agent.executor.ollama

import agent.executor.ExecutorInfo
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.ollama.client.OllamaModels

class Grok8BExecutor : ExecutorInfo {
    override val executor = simpleOllamaAIExecutor()
    override val llmModel = OllamaModels.Groq.LLAMA_3_GROK_TOOL_USE_8B
}