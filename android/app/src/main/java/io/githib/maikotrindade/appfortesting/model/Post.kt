package io.githib.maikotrindade.appfortesting.model

data class Post(
    val user: User,
    val mediaUrl: String? = null,
    val description: String? = null,
    val likes: Int = 0,
    val comments: Int = 0,
    val isLiked: Boolean = false
)
