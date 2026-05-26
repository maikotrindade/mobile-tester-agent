package agent.executor.ollama

import agent.executor.ExecutorInfo
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.ollama.client.OllamaModels

class QWEN36BExecutor : ExecutorInfo {
    override val executor = simpleOllamaAIExecutor()
    override val llmModel = OllamaModels.Alibaba.QWEN_3_06B
}