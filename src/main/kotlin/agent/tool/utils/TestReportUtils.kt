package agent.tool.utils

import agent.tool.utils.MediaUtils.dotenv
import testing.TestResult
import testing.TestScenario
import java.io.File

private val homePath = dotenv["HOME_PATH"] ?: IllegalStateException("Home path is not set")
private val reportPath = "$homePath/mobileTester"

object TestReportUtils {
    fun generateTestReport(scenario: TestScenario): String {
        return try {
            val sb = StringBuilder()
            sb.appendLine("<html><head><title>Test Report: ${scenario.goal}</title></head><body>")
            sb.appendLine("<h1>Test Report: ${scenario.goal}</h1>")
            sb.appendLine("<b>Scenario ID:</b> ${scenario.id}<br>")
            scenario.createdBy?.let { sb.appendLine("<b>Created By:</b> $it<br>") }
            scenario.dateStart?.let { sb.appendLine("<b>Start:</b> $it<br>") }
            scenario.dateEnd?.let { sb.appendLine("<b>End:</b> $it<br>") }
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
            if (scenario.errorMessage.isNotEmpty()) {
                sb.appendLine("<h2>Error:</h2>")
                sb.appendLine("<p>${scenario.errorMessage}</p")
            }
            sb.appendLine("<h2>Result: ${formatResult(scenario.result)}</h2>")
            sb.appendLine("</body></html>")
            val html = sb.toString()
            File("$reportPath/test-report.html").writeText(html)
            "Test report generated successfully at $reportPath/test-report.html"
        } catch (e: Exception) {
            "Failed to generate test report: ${e.message}"
        }
    }

    private fun formatResult(result: TestResult): String = when (result) {
        is TestResult.Passed -> "<span style=\"color:green; font-weight:bold;\">Passed</span>"
        is TestResult.Failed -> "<span style=\"color:red; font-weight:bold;\">Failed: ${result.description}</span>"
        is TestResult.Pending -> "<span style=\"color:orange; font-weight:bold;\">Pending</span>"
        is TestResult.Skipped -> "<span style=\"color:gray; font-weight:bold;\">Skipped: ${result.description}</span>"
        is TestResult.Error -> "<span style=\"color:darkred; font-weight:bold;\">Error: ${result.description}</span>"
    }
}