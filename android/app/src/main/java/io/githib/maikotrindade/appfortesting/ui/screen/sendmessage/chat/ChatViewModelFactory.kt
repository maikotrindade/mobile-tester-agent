package io.githib.maikotrindade.appfortesting.ui.screen.sendmessage.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.githib.maikotrindade.appfortesting.model.User

class ChatViewModelFactory(private val user: User) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(user) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

