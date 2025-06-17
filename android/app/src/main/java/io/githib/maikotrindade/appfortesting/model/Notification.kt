package io.githib.maikotrindade.appfortesting.model

import java.util.UUID

data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val user: User,
    val action: String,
    val time: String
)

