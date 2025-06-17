package io.githib.maikotrindade.appfortesting.ui.screen.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.githib.maikotrindade.appfortesting.model.User
import io.githib.maikotrindade.appfortesting.ui.screen.feed.PostTile
import io.githib.maikotrindade.appfortesting.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    val query by viewModel.query.collectAsState()
    val filteredUsers by viewModel.filteredUsers.collectAsState()
    val filteredPosts by viewModel.filteredPosts.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.updateQuery(it) },
            label = { Text(stringResource(id = R.string.search_users_or_posts)) },
            modifier = Modifier.fillMaxWidth()
        )
        if (loading) {
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(id = R.string.users), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(filteredUsers) { user ->
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        UserAvatar(user)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(user.username, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(id = R.string.posts), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            LazyColumn {
                items(filteredPosts) { post ->
                    PostTile(
                        user = post.user,
                        gifUrl = post.mediaUrl.orEmpty(),
                        description = post.description.orEmpty(),
                        isLiked = post.isLiked,
                        onLikeClick = { viewModel.toggleLike(post) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserAvatar(user: User, size: Int = 36) {
    AsyncImage(
        model = user.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${user.username}&background=random&rounded=true",
        contentDescription = "User Thumbnail",
        modifier = Modifier.size(size.dp).clip(MaterialTheme.shapes.small)
    )
}
