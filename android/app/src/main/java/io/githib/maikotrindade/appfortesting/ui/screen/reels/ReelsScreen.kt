package io.githib.maikotrindade.appfortesting.ui.screen.reels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.githib.maikotrindade.appfortesting.R
import io.githib.maikotrindade.appfortesting.repository.Repository
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen() {
    val gifs = rememberRandomGifs(30)
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.reels),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.reels_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gifs.size) { index ->
                AsyncImage(
                    model = gifs[index],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    alignment = androidx.compose.ui.Alignment.Center
                )
            }
        }
    }
}

@Composable
private fun rememberRandomGifs(count: Int): List<String> {
    return List(count) { Repository.mediaUrls.random(Random(System.currentTimeMillis() + it)) }
}
