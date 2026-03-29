package com.example.hackernews_client.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hackernews_client.api.FirebaseHN
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.ui.theme.ExposedDropdownMenu
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.net.URL

enum class StoryType(val label: String) {
    TOP("Top Stories"),
    NEW("New Stories"),
    JOBS("Job Stories")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedType by remember { mutableStateOf(StoryType.TOP) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    
    val stories = remember { mutableStateListOf<HNItem>() }
    var allStoryIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isInitialLoading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    val pageSize = 20

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisibleItemIndex >= stories.size - 5 && !isLoading && stories.isNotEmpty() && stories.size < allStoryIds.size
        }
    }

    // Load initial IDs and first page when type changes
    LaunchedEffect(selectedType) {
        isInitialLoading = true
        stories.clear()
        allStoryIds = emptyList()
        try {
            allStoryIds = when (selectedType) {
                StoryType.TOP -> FirebaseHN.service.getTopStories()
                StoryType.NEW -> FirebaseHN.service.getNewStories()
                StoryType.JOBS -> FirebaseHN.service.getJobStories()
            }
            val initialIds = allStoryIds.take(pageSize)
            coroutineScope {
                val loadedStories = initialIds.map { id ->
                    async {
                        try { FirebaseHN.service.getItem(id) } catch (e: Exception) { null }
                    }
                }.awaitAll().filterNotNull()
                stories.addAll(loadedStories)
            }
        } catch (e: Exception) {
            Log.e("MainScreen", "Initial load error", e)
        } finally {
            isInitialLoading = false
        }
    }

    // Load more stories
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            isLoading = true
            try {
                coroutineScope {
                    val currentSize = stories.size
                    val nextIds = allStoryIds.drop(currentSize).take(pageSize)
                    val loadedStories = nextIds.map { id ->
                        async {
                            try { FirebaseHN.service.getItem(id) } catch (e: Exception) { null }
                        }
                    }.awaitAll().filterNotNull()
                    stories.addAll(loadedStories)
                }
            } catch (e: CancellationException) {
            } catch (e: Exception) {
                Log.e("MainScreen", "Error loading more stories", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExposedDropdownMenu(
                        options = StoryType.entries.map { it.label },
                        selectedIndex = StoryType.entries.indexOf(selectedType),
                        onSelected = { index ->
                            selectedType = StoryType.entries[index]
                            isMenuExpanded = false
                        }
                    )
                },
                actions = {
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            if (isInitialLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(stories, key = { it.id }) { story ->
                        StoryItem(story)
                    }

                    if (isLoading) {
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

@Composable
fun StoryItem(story: HNItem) {
    val storyUrl = remember(story.url) {
        try {
            URL(story.url).host
        } catch (e: Exception) {
            ""
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                modifier = Modifier.width(100.dp),
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

