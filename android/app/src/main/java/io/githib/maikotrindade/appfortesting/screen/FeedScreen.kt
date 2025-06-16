@file:OptIn(ExperimentalMaterial3Api::class)

package io.githib.maikotrindade.appfortesting.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import io.githib.maikotrindade.appfortesting.component.StoryItem
import io.githib.maikotrindade.appfortesting.model.Story
import io.githib.maikotrindade.appfortesting.repository.Repository.posts
import io.githib.maikotrindade.appfortesting.repository.Repository.stories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    StoriesSection(stories = stories)
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
        items(posts) { post ->
            PostTile(
                user = post.user,
                gifUrl = post.mediaUrl.orEmpty()
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun StoriesSection(stories: List<Story>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(stories) { story ->
            StoryItem(story = story)
        }
    }
}

@Composable
fun PostTile(
    user: io.githib.maikotrindade.appfortesting.model.User,
    gifUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(ImageDecoderDecoder.Factory())
        }
        .build()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(12.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = "User Thumbnail",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gifUrl)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = "GIF Post",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* handle click */ }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Like")
            }
            IconButton(onClick = { /* handle click */ }) {
                Icon(Icons.Default.Email, contentDescription = "Comment")
            }
            IconButton(onClick = { /* handle click */ }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    }
}
