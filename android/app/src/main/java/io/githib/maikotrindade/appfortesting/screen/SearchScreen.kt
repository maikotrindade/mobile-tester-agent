package io.githib.maikotrindade.appfortesting.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.githib.maikotrindade.appfortesting.model.User
import io.githib.maikotrindade.appfortesting.repository.Repository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    val users = Repository.stories.map { it.user }
    val posts = Repository.posts

    val filteredUsers = users.filter { it.username.contains(query, ignoreCase = true) }
    val filteredPosts = posts.filter { it.user.username.contains(query, ignoreCase = true) || (it.mediaUrl?.contains(query, ignoreCase = true) == true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search users or posts") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Users", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(filteredUsers) { user ->
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    io.githib.maikotrindade.appfortesting.screen.UserAvatar(user)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(user.username, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Posts", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        LazyColumn {
            items(filteredPosts) { post ->
                PostTile(user = post.user, gifUrl = post.mediaUrl.orEmpty())
            }
        }
    }
}

@Composable
fun UserAvatar(user: User, size: Int = 36) {
    coil.compose.AsyncImage(
        model = user.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${user.username}&background=random&rounded=true",
        contentDescription = "User Thumbnail",
        modifier = Modifier.size(size.dp).clip(MaterialTheme.shapes.small)
    )
}
