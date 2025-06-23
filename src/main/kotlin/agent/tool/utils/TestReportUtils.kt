package agent.tool.utils

import testing.TestResult
import testing.TestScenario
import java.io.File

const val reportPath = "/home/maiko/test"

object TestReportUtils {
    fun generateTestReport(scenario: TestScenario) {
        val sb = StringBuilder()
        sb.appendLine("<html><head><title>Test Report: ${scenario.goal}</title></head><body>")
        sb.appendLine("<h1>Test Report: ${scenario.goal}</h1>")
        sb.appendLine("<b>Scenario ID:</b> ${scenario.id}<br>")
        scenario.createdBy?.let { sb.appendLine("<b>Created By:</b> $it<br>") }
        scenario.dateStart?.let { sb.appendLine("<b>Start:</b> $it<br>") }
        scenario.dateEnd?.let { sb.appendLine("<b>End:</b> $it<br>") }
        sb.appendLine("<b>Tags:</b> ${scenario.tags.joinToString(", ")}<br>")
        scenario.notes?.let { sb.appendLine("<b>Notes:</b> $it<br>") }
        scenario.videoPath?.let { sb.appendLine("<b>Video:</b> $it<br>") }
        sb.appendLine("<h2>Steps:</h2><ol>")
        scenario.testSteps.forEachIndexed { idx, step ->
            sb.appendLine("<li>${step.description}<ul>")
            sb.appendLine("<li><b>Expected:</b> ${step.expectedOutcome}</li>")
            sb.appendLine("<li><b>Actual:</b> ${step.actualOutcome ?: "N/A"}</li>")
            sb.appendLine("<li><b>Status:</b> ${step.status}</li>")
            step.error?.let { sb.appendLine("<li><b>Error:</b> $it</li>") }
            step.screenshotPath?.let { sb.appendLine("<li><b>Screenshot:</b> $it</li>") }
            sb.appendLine("</ul></li>")
        }
        sb.appendLine("</ol>")
        if (scenario.errors.isNotEmpty()) {
            sb.appendLine("<h2>Errors:</h2><ul>")
            scenario.errors.forEach { sb.appendLine("<li>$it</li>") }
            sb.appendLine("</ul>")
        }
        sb.appendLine("<h2>Result: ${formatResult(scenario.result)}</h2>")
        sb.appendLine("</body></html>")
        val html = sb.toString()
        File("$reportPath/test-report.html").writeText(html)
    }

    private fun formatResult(result: TestResult): String = when (result) {
        is TestResult.Passed -> "Passed"
        is TestResult.Failed -> "Failed: ${result.description}"
        is TestResult.Pending -> "Pending"
        is TestResult.Skipped -> "Skipped: ${result.description}"
        is TestResult.Error -> "Error: ${result.description}"
    }
}