package agent.tool.reporting.utils

import agent.model.TestScenarioReport
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TestReportUtils {
    fun getCurrentFormattedDateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }

    fun generateTestReport(scenario: TestScenarioReport): String {
        return try {
            val reportBuilder = StringBuilder()
            reportBuilder.appendLine("Test Report: ${scenario.goal}")
            scenario.dateStart?.let { reportBuilder.appendLine("Start: $it") }
            scenario.dateEnd?.let { reportBuilder.appendLine("End:</b> $it") }
            reportBuilder.appendLine("\nSteps:")
            scenario.testSteps.forEachIndexed { idx, step ->
                reportBuilder.appendLine("\n${step.description}")
            }
            reportBuilder.toString()
        } catch (e: Exception) {
            "Failed to generate test report: ${e.message}"
        }
    }
}