package agent.executor

import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.OllamaModels

class OllamaGwenExecutor : ExecutorInfo {
    override val executor = simpleOllamaAIExecutor()
    override val llmModel = OllamaModels.Alibaba.QWEN_3_06B
}