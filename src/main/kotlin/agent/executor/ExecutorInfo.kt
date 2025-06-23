package agent.executor

import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel

interface ExecutorInfo {
    val executor: PromptExecutor
    val llmModel: LLModel
}