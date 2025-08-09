package agent.tool.mobile.test.utils

import kotlinx.serialization.Serializable

/**
 * Data class representing a UI element match result for serialization.
 * Contains the group values captured by the regex.
 */
@Serializable
data class UiMatchResult(val groupValues: List<String>)