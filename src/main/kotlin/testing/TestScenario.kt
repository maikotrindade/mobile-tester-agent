package testing

import kotlinx.serialization.Serializable

/**
 * Represents a test scenario with its goal, steps, execution times, errors, and result.
 */
@Serializable
data class TestScenario(
    val id: String,
    val goal: String,
    val testSteps: List<TestStep>,
    val dateStart: String? = null,
    val dateEnd: String? = null,
    val errors: List<String> = emptyList(),
    val result: TestResult = TestResult.Pending,
    val tags: List<String> = emptyList(),
    val createdBy: String? = null,
    val lastModified: String? = null,
    val notes: String? = null,
    val videoPath: String? = null,
)
