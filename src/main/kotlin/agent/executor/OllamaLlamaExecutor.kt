package agent.executor

import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.OllamaModels

class OllamaLlamaExecutor : ExecutorInfo {
    override val executor = simpleOllamaAIExecutor()
    override val llmModel = OllamaModels.Meta.LLAMA_3_2_3B
}