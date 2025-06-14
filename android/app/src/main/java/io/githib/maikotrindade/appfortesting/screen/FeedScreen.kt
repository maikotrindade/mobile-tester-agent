@file:OptIn(ExperimentalMaterial3Api::class)

package io.githib.maikotrindade.appfortesting.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.githib.maikotrindade.appfortesting.component.StoryItem
import io.githib.maikotrindade.appfortesting.model.Story

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val stories = listOf(
        Story("Your story", null, hasStory = false),
        Story("user1", null),
        Story("user2", null),
        Story("user3", null)
    )
        StoriesSection(stories = stories)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.Gray)
        ) {
            Text(
                text = "Post Content Placeholder",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
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
