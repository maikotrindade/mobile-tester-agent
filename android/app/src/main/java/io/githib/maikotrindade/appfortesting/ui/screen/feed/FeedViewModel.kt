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

    private var currentPosts: MutableList<Post> = mutableListOf()

    init {
        loadFeed()
    }

    fun loadFeed() {
        _loading.value = true
        viewModelScope.launch {
            delay(1200) // fake loading
            // Always update currentPosts from Repository if it's empty, otherwise keep new posts
            if (currentPosts.isEmpty()) {
                currentPosts = Repository.posts.toMutableList()
            } else {
                // If Repository.posts has new posts (e.g. after app restart), merge them without duplicates
                val repoPosts = Repository.posts.filter { repoPost ->
                    currentPosts.none { it.description == repoPost.description && it.mediaUrl == repoPost.mediaUrl && it.user.username == repoPost.user.username }
                }
                currentPosts.addAll(repoPosts)
            }
            _posts.value = currentPosts
            _stories.value = Repository.stories
            _loading.value = false
        }
    }

    fun refresh() {
        loadFeed()
    }

    fun addPost(post: Post) {
        Repository.posts.add(0, post)
        refresh()
    }
}
