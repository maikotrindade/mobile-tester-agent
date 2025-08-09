package agent.model

import kotlinx.serialization.Serializable

/**
 * Represents a test scenario with a specific goal and a sequence of test steps.
 *
 * @property goal The objective or purpose of this test scenario.
 * @property testSteps The list of steps to execute for this test scenario.
 * @property dateStart The date and time when the test scenario was started.
 * @property dateEnd The date and time when the test scenario was finished.
 */
@Serializable
data class TestScenarioReport(
    var goal: String,
    var testSteps: MutableList<TestStep>,
    var dateStart: String? = null,
    var dateEnd: String? = null,
)
