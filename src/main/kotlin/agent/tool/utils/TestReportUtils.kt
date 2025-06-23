package agent.tool.utils

import testing.TestResult
import testing.TestScenario
import java.time.format.DateTimeFormatter

object TestReportUtils {
    fun generateTestReport(scenario: TestScenario): String {
        val sb = StringBuilder()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        sb.appendLine("# Test Report: ${scenario.goal}")
        sb.appendLine("**Scenario ID:** ${scenario.id}")
        scenario.createdBy?.let { sb.appendLine("**Created By:** $it") }
        scenario.dateStart?.let { sb.appendLine("**Start:** ${it.format(dateFormatter)}") }
        scenario.dateEnd?.let { sb.appendLine("**End:** ${it.format(dateFormatter)}") }
        sb.appendLine("**Tags:** ${scenario.tags.joinToString(", ")}")
        scenario.notes?.let { sb.appendLine("**Notes:** $it") }
        scenario.videoPath?.let { sb.appendLine("**Video:** $it") }
        sb.appendLine("\n## Steps:")
        scenario.testSteps.forEachIndexed { idx, step ->
            sb.appendLine("${idx + 1}. ${step.description}")
            sb.appendLine("   - Expected: ${step.expectedOutcome}")
            sb.appendLine("   - Actual: ${step.actualOutcome ?: "N/A"}")
            sb.appendLine("   - Status: ${step.status}")
            step.error?.let { sb.appendLine("   - Error: $it") }
            step.screenshotPath?.let { sb.appendLine("   - Screenshot: $it") }
        }
        if (scenario.errors.isNotEmpty()) {
            sb.appendLine("\n## Errors:")
            scenario.errors.forEach { sb.appendLine("- $it") }
        }
        sb.appendLine("\n## Result: ${formatResult(scenario.result)}")
        return sb.toString()
    }

    private fun formatResult(result: TestResult): String = when (result) {
        is TestResult.Passed -> "Passed"
        is TestResult.Failed -> "Failed: ${result.description}"
        is TestResult.Pending -> "Pending"
        is TestResult.Skipped -> "Skipped: ${result.description}"
        is TestResult.Error -> "Error: ${result.description}"
    }
}