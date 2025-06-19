package io.github.maikotrindade.agent.executor.agent.executor

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import io.github.cdimascio.dotenv.dotenv

class GeminiExecutor : ExecutorInfo {
    val dotenv = dotenv()
    override val executor = simpleGoogleAIExecutor(dotenv["GEMINI_API_KEY"])
    override val llmModel = GoogleModels.Gemini2_0Flash
}