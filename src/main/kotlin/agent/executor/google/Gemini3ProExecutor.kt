package agent.executor.google

import agent.executor.ExecutorInfo
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import io.github.cdimascio.dotenv.dotenv

class Gemini3ProExecutor : ExecutorInfo {
    val dotenv = dotenv()
    override val executor = simpleGoogleAIExecutor(dotenv["GEMINI_API_KEY"])
    override val llmModel = GoogleModels.Gemini3_Pro_Preview
}