package io.githib.maikotrindade.appfortesting.ui.screen.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.githib.maikotrindade.appfortesting.model.Post
import io.githib.maikotrindade.appfortesting.model.Story
import io.githib.maikotrindade.appfortesting.repository.Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        _loading.value = true
        viewModelScope.launch {
            delay(1200) // fake loading
            _posts.value = Repository.posts
            _stories.value = Repository.stories
            _loading.value = false
        }
    }

    fun refresh() {
        loadFeed()
    }
}

