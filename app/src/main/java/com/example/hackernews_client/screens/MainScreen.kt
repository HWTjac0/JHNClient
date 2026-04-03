package com.example.hackernews_client.screens

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.hackernews_client.R
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.ui.StoryItem
import com.example.hackernews_client.ui.TagInputDialog
import com.example.hackernews_client.ui.theme.ExposedDropdownMenu
import com.example.hackernews_client.viemodels.MainViewModel
import java.net.URL

enum class StoryType(@StringRes val label: Int) {
    TOP(R.string.top_stories),
    NEW(R.string.new_stories),
    JOBS(R.string.job_stories)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val items = viewModel.stories.collectAsLazyPagingItems()
    val selectedType by viewModel.selectedType.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    var storyToSave by remember { mutableStateOf<HNItem?>(null) }

    if (storyToSave != null) {
        TagInputDialog(
            tags = allTags,
            onConfirm = { tags ->
                storyToSave?.let { viewModel.saveStory(it, tags) }
                storyToSave = null
            },
            onDismiss = { storyToSave = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExposedDropdownMenu(
                        options = StoryType.entries.map { stringResource(it.label) },
                        selectedIndex = StoryType.entries.indexOf(selectedType),
                        onSelected = { index ->
                            viewModel.onTypeSelected(StoryType.entries[index])
                        }
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (items.loadState.refresh) {
                is LoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is LoadState.Error -> {
                    val error = items.loadState.refresh as LoadState.Error
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error.error.localizedMessage ?: stringResource(R.string.unknown_error),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Button(
                            onClick = { items.retry() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = items.itemCount,
                            key = items.itemKey { it.id },
                        ) { index ->
                            val story = items[index]
                            if (story != null) {
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.StartToEnd) {
                                            storyToSave = story
                                        }
                                        false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromEndToStart = false,
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
                                        onClick = { onStoryClick(story.id) },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                    )
                                }
                            }
                        }

                        when (val state = items.loadState.append) {
                            is LoadState.Loading -> {
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

                            is LoadState.Error -> {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = state.error.localizedMessage ?: stringResource(R.string.error_loading_more),
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                        Button(
                                            onClick = { items.retry() },
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            Text(stringResource(R.string.retry))
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
