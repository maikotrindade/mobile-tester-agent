package io.github.maikotrindade.agent.executor.agent.executor

import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OllamaModels

class OllamaGwenExecutor : ExecutorInfo {
    override val executor = simpleOllamaAIExecutor()
    override val llmModel = LLModel(
        id = OllamaModels.Alibaba.QWEN_2_5_05B.id,
        provider = LLMProvider.Ollama,
        capabilities = listOf(
            LLMCapability.Temperature,
            LLMCapability.Schema.JSON.Simple,
            LLMCapability.Tools
        )
    )
}