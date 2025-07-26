package agent.model

import kotlinx.serialization.Serializable

/**
 * Represents a single test step in a scenario.
 */
@Serializable
data class TestStep(
    var description: String,
    var expectedOutcome: String,
    var actualOutcome: String? = null,
    var status: StepStatus = StepStatus.PENDING,
    var error: String? = null,
    var screenshotPath: String? = null
)

@Serializable
enum class StepStatus {
    PENDING, PASSED, FAILED, SKIPPED
}
