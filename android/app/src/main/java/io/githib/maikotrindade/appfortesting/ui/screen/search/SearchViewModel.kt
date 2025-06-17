package io.githib.maikotrindade.appfortesting.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.githib.maikotrindade.appfortesting.model.Post
import io.githib.maikotrindade.appfortesting.model.User
import io.githib.maikotrindade.appfortesting.repository.Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers.asStateFlow()

    private val _filteredPosts = MutableStateFlow<List<Post>>(emptyList())
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val users = Repository.stories.map { it.user }
    private val posts = Repository.posts

    private var searchJob: Job? = null

    init {
        updateQuery("")
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        _loading.value = true
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(1800)
            _filteredUsers.value = users.filter { it.username.contains(newQuery, ignoreCase = true) }
            _filteredPosts.value = posts.filter {
                it.user.username.contains(newQuery, ignoreCase = true) ||
                (it.mediaUrl?.contains(newQuery, ignoreCase = true) == true) ||
                (it.description?.contains(newQuery, ignoreCase = true) == true)
            }
            _loading.value = false
        }
    }
}