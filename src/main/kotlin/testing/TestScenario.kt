package testing

import java.time.LocalDateTime

/**
 * Represents a test scenario with its goal, steps, execution times, errors, and result.
 */
data class TestScenario(
    val id: String,
    val goal: String,
    val testSteps: List<TestStep>,
    val dateStart: LocalDateTime? = null,
    val dateEnd: LocalDateTime? = null,
    val errors: List<String> = emptyList(),
    val result: TestResult = TestResult.Pending,
    val tags: List<String> = emptyList(),
    val createdBy: String? = null,
    val lastModified: LocalDateTime? = null,
    val notes: String? = null,
    val videoPath: String? = null
)
