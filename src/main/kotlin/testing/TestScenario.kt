package testing

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Represents a test scenario with its goal, steps, execution times, errors, and result.
 */
@Serializable
data class TestScenario(
    var id: String = UUID.randomUUID().toString(),
    var goal: String,
    var testSteps: List<TestStep>,
    var dateStart: String? = null,
    var dateEnd: String? = null,
    var errorMessage: String,
    var result: TestResult = TestResult.Pending,
    var createdBy: String? = null,
    var lastModified: String? = null,
    var notes: String? = null,
    var videoPath: String? = null,
)
