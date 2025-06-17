package io.githib.maikotrindade.appfortesting.ui.screen.sendmessage.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.githib.maikotrindade.appfortesting.model.ChatMessage
import io.githib.maikotrindade.appfortesting.model.User

class ChatViewModel(user: User) : ViewModel() {
    var messageText by mutableStateOf("")
        private set
    var messages by mutableStateOf(
        listOf(
            ChatMessage("Hey ${user.username}! 👋", false),
            ChatMessage("Hi! How are you?", true),
            ChatMessage("I'm good, thanks! What about you?", false),
            ChatMessage("Doing great!", true),
            ChatMessage("Let's catch up soon.", false),
            ChatMessage("Sure! 😊", true)
        )
    )
        private set

    fun onMessageTextChanged(newText: String) {
        messageText = newText
    }

    fun sendMessage() {
        if (messageText.isNotBlank()) {
            messages = messages + ChatMessage(messageText, true)
            messageText = ""
        }
    }
}

