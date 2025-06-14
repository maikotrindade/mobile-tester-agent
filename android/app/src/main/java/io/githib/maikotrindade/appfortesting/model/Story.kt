package io.githib.maikotrindade.appfortesting.model

data class Story(
    val username: String,
    val profileImage: Int?, // Resource ID for profile image
    val hasStory: Boolean = true,
    val isLive: Boolean = false
)
