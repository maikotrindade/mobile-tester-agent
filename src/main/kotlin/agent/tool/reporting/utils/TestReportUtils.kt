package agent.tool.reporting.utils

import agent.model.TestScenarioReport
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility object for generating test reports and formatting date-time values.
 */
object TestReportUtils {

    /**
     * Retrieves the current date and time formatted as a string.
     *
     * @return A string representing the current date and time in the format "yyyy-MM-dd HH:mm:ss".
     */
    fun getCurrentFormattedDateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }

    /**
     * Generates a detailed test report for a given test scenario.
     *
     * @param scenario The test scenario report containing details such as goal, start date, end date, and test steps.
     * @return A string representation of the test report, or an error message if report generation fails.
     */
    fun generateTestReport(scenario: TestScenarioReport): String {
        return try {
            val reportBuilder = StringBuilder()
            reportBuilder.appendLine("Test Report: ${scenario.goal}")
            scenario.dateStart?.let { reportBuilder.appendLine("Start: $it") }
            scenario.dateEnd?.let { reportBuilder.appendLine("End:</b> $it") }
            reportBuilder.appendLine("\nSteps:")
            scenario.testSteps.forEach {
                reportBuilder.appendLine("\n$it")
            }
            reportBuilder.toString()
        } catch (e: Exception) {
            "Failed to generate test report: ${e.message}"
        }
    }
}