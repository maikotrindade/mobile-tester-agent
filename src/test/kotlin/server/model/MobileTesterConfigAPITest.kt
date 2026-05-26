package server.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MobileTesterConfigAPITest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `defaults match documented values`() {
        val cfg = MobileTesterConfigAPI()
        assertEquals("Gemini3Pro", cfg.executorInfoId)
        assertEquals(0.0, cfg.llmTemperature)
        assertEquals(50, cfg.maxAgentIterations)
        assertEquals(false, cfg.logTokensConsumption)
    }

    @Test
    fun `deserializes partial payload using defaults for missing fields`() {
        val cfg = json.decodeFromString<MobileTesterConfigAPI>("""{"executorInfoId":"Opus47"}""")
        assertEquals("Opus47", cfg.executorInfoId)
        assertEquals(50, cfg.maxAgentIterations)
    }

    @Test
    fun `toMobileConfig throws on unknown executorInfoId`() {
        val cfg = MobileTesterConfigAPI(executorInfoId = "DoesNotExist")
        val ex = assertFailsWith<IllegalArgumentException> { cfg.toMobileConfig() }
        assert(ex.message!!.contains("DoesNotExist"))
    }

    @Test
    fun `toMobileConfig is case-sensitive on executorInfoId`() {
        val cfg = MobileTesterConfigAPI(executorInfoId = "gemini3pro")
        assertFailsWith<IllegalArgumentException> { cfg.toMobileConfig() }
    }
}
