package testing

sealed class TestResult {
    object Passed : TestResult()
    data class Failed(val description: String) : TestResult()
    object Pending : TestResult()
    data class Skipped(val description: String) : TestResult()
    data class Error(val description: String) : TestResult()
}