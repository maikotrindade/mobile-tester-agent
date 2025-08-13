package agent.tool.mobile.test.utils

import kotlinx.serialization.Serializable

/**
 * Represents the result of a UI match operation.
 *
 * @property groupValues A list of strings containing the group values captured by the regex.
 */
@Serializable
data class UiMatchResult(val groupValues: List<String>)