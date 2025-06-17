package io.githib.maikotrindade.appfortesting.ui.screen.sendmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.githib.maikotrindade.appfortesting.model.User
import io.githib.maikotrindade.appfortesting.repository.Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SendMessageViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _users.value = emptyList()
            delay(1200) // fake loading
            _users.value = Repository.getAllUsers()
        }
    }
}
