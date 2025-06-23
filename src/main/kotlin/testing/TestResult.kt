package testing

import kotlinx.serialization.Serializable

@Serializable
sealed class TestResult {
    @Serializable
    object Passed : TestResult()

    @Serializable
    data class Failed(val description: String) : TestResult()

    @Serializable
    object Pending : TestResult()

    @Serializable
    data class Skipped(val description: String) : TestResult()

    @Serializable
    data class Error(val description: String) : TestResult()
}