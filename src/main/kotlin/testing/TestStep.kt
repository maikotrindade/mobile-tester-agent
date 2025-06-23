package testing

import kotlinx.serialization.Serializable

/**
 * Represents a single test step in a scenario.
 */
@Serializable
data class TestStep(
    val description: String,
    val expectedOutcome: String,
    val actualOutcome: String? = null,
    val status: StepStatus = StepStatus.PENDING,
    val error: String? = null,
    val screenshotPath: String? = null
)

@Serializable
enum class StepStatus {
    PENDING, PASSED, FAILED, SKIPPED
}
