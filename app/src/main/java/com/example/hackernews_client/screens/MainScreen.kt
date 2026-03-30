package com.example.hackernews_client.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.ui.theme.ExposedDropdownMenu
import com.example.hackernews_client.viemodels.MainUiState
import com.example.hackernews_client.viemodels.MainViewModel
import java.net.URL

enum class StoryType(val label: String) {
    TOP("Top Stories"),
    NEW("New Stories"),
    JOBS("Job Stories")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val isLoadingMore by viewModel.isLoading.collectAsState()
    
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExposedDropdownMenu(
                        options = StoryType.entries.map { it.label },
                        selectedIndex = StoryType.entries.indexOf(selectedType),
                        onSelected = { index ->
                            viewModel.onTypeSelected(StoryType.entries[index])
                        }
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MainUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(
                            onClick = { viewModel.retry() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is MainUiState.Success -> {
                    val stories = state.stories
                    
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                            lastVisibleItemIndex >= stories.size - 5
                        }
                    }

                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value) {
                            viewModel.loadMoreStories()
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(stories, key = { _, story -> story.id }) { index, story ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.StartToEnd) {
                                        // Tutaj można dodać akcję 'like'
                                    }
                                    false // Zawsze zwracaj false, aby element wrócił na miejsce
                                }
                            )
                            
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromEndToStart = false, // Wyłącz swipe w lewo
                                backgroundContent = {
                                    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                                        MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                            Icon(Icons.Default.Favorite, contentDescription = "Like")
                                        }
                                    }
                                }
                            ) {
                                StoryItem(
                                    story = story, 
                                    index = index, 
                                    onClick = { onStoryClick(story.id)},
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                )
                            }
                        }

                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryItem(
    story: HNItem,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storyUrl = remember(story.url) {
        try {
            URL(story.url).host
        } catch (e: Exception) {
            ""
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                modifier = Modifier.width(34.dp)
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = story.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "By: ${story.by ?: "Unknown"} | Score: ${story.score ?: 0}${if (storyUrl.isNotEmpty()) " | $storyUrl" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(
                modifier = Modifier.width(75.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "💬 ${story.kids?.size ?: 0}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
