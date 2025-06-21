package testing

/**
 * Represents a single test step in a scenario.
 */
data class TestStep(
    val description: String,
    val expectedOutcome: String,
    val actualOutcome: String? = null,
    val status: StepStatus = StepStatus.PENDING,
    val error: String? = null,
    val screenshotPath: String? = null
)

enum class StepStatus {
    PENDING, PASSED, FAILED, SKIPPED
}

