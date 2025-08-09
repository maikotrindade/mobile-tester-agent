package agent.tool.reporting

import agent.model.TestScenarioReport
import agent.tool.reporting.utils.TestReportUtils
import agent.tool.reporting.utils.TestReportUtils.getCurrentFormattedDateTime
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

class ReportingTools : ToolSet {

    private var testScenarioReport: TestScenarioReport? = null

    @Tool
    @LLMDescription(
        "Initializes a new test scenario report with a specified goal. " +
                "This should be the first tool called in any test plan."
    )
    suspend fun initializeTestScenarioReport(goal: String): String {
        testScenarioReport = TestScenarioReport(
            goal = goal,
            testSteps = mutableListOf(),
            dateStart = getCurrentFormattedDateTime()
        )
        return "Test scenario report created with goal: $goal"
    }

    @Tool
    @LLMDescription("Generate a test report for a given TestScenario in the end of the test.")
    suspend fun generateTestScenarioReport(): String {
        return try {
            testScenarioReport?.dateEnd = getCurrentFormattedDateTime()
            TestReportUtils.generateTestReport(testScenarioReport!!)
        } catch (e: Exception) {
            "Failed to parse scenario JSON: ${e.message}"
        }
    }

    @Tool
    @LLMDescription("Update the scenario report by adding a test step to the current test scenario.")
    suspend fun updateTestScenarioReport(description: String): String {
        testScenarioReport?.testSteps?.add(description)
        return "Test step added: $description"
    }
}