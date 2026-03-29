package com.example.hackernews_client.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
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

    LaunchedEffect(Unit) {
        try {
            allStoryIds = FirebaseHN.service.getTopStories()
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

    Box(modifier = modifier.fillMaxSize()) {
        if (isInitialLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(stories, key = { _, story -> story.id }) { _, story ->
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

@Composable
fun StoryItem(story: HNItem) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = story.title ?: "No Title",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "By: ${story.by ?: "Unknown"} | Score: ${story.score ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}