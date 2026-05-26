package server.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AgentRequestTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserializes a well-formed request`() {
        val payload = """
            {"goal":"Login","packageName":"com.app","steps":["open","tap login"]}
        """.trimIndent()

        val req = json.decodeFromString<AgentRequest>(payload)

        assertEquals("Login", req.goal)
        assertEquals("com.app", req.packageName)
        assertEquals(listOf("open", "tap login"), req.steps)
    }

    @Test
    fun `serializes round-trip preserves fields`() {
        val original = AgentRequest(goal = "Buy", packageName = "com.shop", steps = listOf("a", "b"))
        val decoded = json.decodeFromString<AgentRequest>(json.encodeToString(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `missing required field fails to deserialize`() {
        val payload = """{"goal":"x","steps":[]}"""
        assertFailsWith<Exception> { json.decodeFromString<AgentRequest>(payload) }
    }
}
