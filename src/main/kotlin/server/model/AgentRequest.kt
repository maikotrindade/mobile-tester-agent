package server.model

import kotlinx.serialization.Serializable

@Serializable
data class AgentRequest(val goal: String, val steps: List<String>)
