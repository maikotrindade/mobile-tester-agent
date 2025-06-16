package io.githib.maikotrindade.appfortesting.model

data class Story(
    val user: User,
    val hasStory: Boolean = true,
    val isLive: Boolean = false
)
