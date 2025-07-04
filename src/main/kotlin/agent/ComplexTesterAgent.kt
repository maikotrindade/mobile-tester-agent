package agent

import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CompletableDeferred

object ComplexTesterAgent {
    suspend fun runAgent(prompt: String): String {
        val dotenv = dotenv()
        val googleClient = GoogleLLMClient(dotenv["GEMINI_API_KEY"])
        val resultDeferred = CompletableDeferred<String>()
        // TODO
        return resultDeferred.await()
    }
}