package com.example.hackernews_client.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.hackernews_client.database.SavedStory
import com.example.hackernews_client.ui.TagInputDialog
import com.example.hackernews_client.viemodels.SavedViewModel
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onStoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedViewModel = viewModel()
) {
    val items = viewModel.savedStories.collectAsLazyPagingItems()
    val allTags by viewModel.allTags.collectAsState()
    val allTagNames by viewModel.allTagNames.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    var storyToEdit by remember { mutableStateOf<SavedStory?>(null) }
    
    val currentStoryTags by remember(storyToEdit) {
        if (storyToEdit != null) {
            viewModel.getTagsForStory(storyToEdit!!.id)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    if (storyToEdit != null) {
        TagInputDialog(
            initialSelectedTags = currentStoryTags,
            tags = allTagNames,
            onConfirm = { tags ->
                storyToEdit?.let { viewModel.updateStoryTags(it, tags) }
                storyToEdit = null
            },
            onDismiss = { storyToEdit = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Stories") }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allTags) { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag.name),
                            onClick = { viewModel.toggleTag(tag.name) },
                            label = {
                                Text(
                                    tag.name,
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = { viewModel.toggleTag(tag.name) },
                                            onLongClick = { viewModel.deleteTag(tag.name) }
                                        )
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val refreshState = items.loadState.refresh) {
                    is LoadState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is LoadState.Error -> {
                        Text(
                            text = refreshState.error.localizedMessage ?: "Error",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        if (items.itemCount == 0) {
                            Text(
                                text = "No saved stories",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(
                                    count = items.itemCount,
                                    key = items.itemKey { it.id }
                                ) { index ->
                                    val story = items[index]
                                    if (story != null) {
                                        val dismissState = rememberSwipeToDismissBoxState(
                                            confirmValueChange = { value ->
                                                when (value) {
                                                    SwipeToDismissBoxValue.EndToStart -> {
                                                        viewModel.deleteStory(story.id)
                                                        true
                                                    }
                                                    SwipeToDismissBoxValue.StartToEnd -> {
                                                        storyToEdit = story
                                                        false
                                                    }
                                                    else -> false
                                                }
                                            }
                                        )

                                        SwipeToDismissBox(
                                            state = dismissState,
                                            backgroundContent = {
                                                val direction = dismissState.dismissDirection
                                                val color = when (direction) {
                                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.Transparent
                                                }
                                                val alignment = when (direction) {
                                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                                    else -> Alignment.Center
                                                }
                                                val icon = when (direction) {
                                                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                                    else -> null
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(color)
                                                        .padding(horizontal = 20.dp),
                                                    contentAlignment = alignment
                                                ) {
                                                    if (icon != null) {
                                                        Icon(icon, contentDescription = null)
                                                    }
                                                }
                                            }
                                        ) {
                                            SavedStoryItem(
                                                story = story,
                                                onClick = { onStoryClick(story.id) },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                            )
                                        }
                                    }
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
fun SavedStoryItem(
    story: SavedStory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = story.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "By: ${story.by} | Score: ${story.score}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
