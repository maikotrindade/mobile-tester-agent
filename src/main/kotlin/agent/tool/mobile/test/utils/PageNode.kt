package agent.tool.mobile.test.utils

import kotlinx.serialization.Serializable

@Serializable
data class PageNode(
    val i: Int,
    val role: String,
    val text: String,
    val desc: String,
    val id: String,
    val childTexts: List<String>,
    val clickable: Boolean,
    val long: Boolean,
    val enabled: Boolean,
    val selected: Boolean,
    val checked: Boolean,
    val boundsLeft: Int,
    val boundsTop: Int,
    val boundsRight: Int,
    val boundsBottom: Int,
    val cx: Int,
    val cy: Int,
    val parent: Int
)
