package agent.model

import kotlinx.serialization.Serializable

/**
 * Represents a test scenario with a specific goal and a sequence of test steps.
 *
 * @property goal The objective or purpose of this test scenario.
 * @property testSteps The list of steps to execute for this test scenario.
 */
@Serializable
data class TestScenario(
    var goal: String,
    var testSteps: List<TestStep>,
    var dateStart: String? = null,
    var dateEnd: String? = null,
//    var errorMessage: String,
//    var result: TestResult = TestResult.Pending,
//    var createdBy: String? = null,
//    var lastModified: String? = null,
//    var notes: String? = null,
//    var videoPath: String? = null,
)
