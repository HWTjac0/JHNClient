package com.example.hackernews_client.screens

import android.util.Log
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hackernews_client.api.AlgoliaHN
import com.example.hackernews_client.api.AlgoliaHit
import com.example.hackernews_client.ui.theme.ExposedDropdownMenu
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.net.URL

enum class SearchSort(val label: String) {
    POPULARITY("Popularity"),
    DATE("Date")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(SearchSort.POPULARITY) }
    
    val hits = remember { mutableStateListOf<AlgoliaHit>() }
    var isLoading by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMorePages by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    // Debounced search
    LaunchedEffect(query, selectedSort) {
        if (query.isBlank()) {
            hits.clear()
            return@LaunchedEffect
        }
        
        delay(500) // Debounce 500ms
        isLoading = true
        currentPage = 0
        hits.clear()
        
        try {
            val response = if (selectedSort == SearchSort.POPULARITY) {
                AlgoliaHN.service.search(query = query, page = 0)
            } else {
                AlgoliaHN.service.searchByDate(query = query, page = 0)
            }
            hits.addAll(response.hits)
            hasMorePages = response.page < response.nbPages - 1
            currentPage = 0
        } catch (e: Exception) {
            Log.e("SearchScreen", "Search error", e)
        } finally {
            isLoading = false
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisibleItemIndex >= hits.size - 5 && !isLoading && hasMorePages && hits.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            isLoading = true
            val nextPage = currentPage + 1
            try {
                val response = if (selectedSort == SearchSort.POPULARITY) {
                    AlgoliaHN.service.search(query = query, page = nextPage)
                } else {
                    AlgoliaHN.service.searchByDate(query = query, page = nextPage)
                }
                hits.addAll(response.hits)
                currentPage = nextPage
                hasMorePages = response.page < response.nbPages - 1
            } catch (e: CancellationException) {
                // Ignore
            } catch (e: Exception) {
                Log.e("SearchScreen", "Load more error", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        placeholder = { Text("Search stories...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                },
                actions = {
                    ExposedDropdownMenu(
                        options = SearchSort.entries.map { it.label },
                        selectedIndex = SearchSort.entries.indexOf(selectedSort),
                        onSelected = { index ->
                            selectedSort = SearchSort.entries[index]
                        },
                        modifier = Modifier.width(150.dp)
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            if (isLoading && hits.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (hits.isEmpty() && query.isNotEmpty() && !isLoading) {
                Text(
                    text = "No results found for \"$query\"",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(hits, key = { _, hit -> hit.objectID }) { _, hit ->
                        SearchHitItem(hit)
                    }

                    if (isLoading && hits.isNotEmpty()) {
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
fun SearchHitItem(hit: AlgoliaHit) {
    val host = remember(hit.url) {
        try {
            URL(hit.url).host
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
                    text = hit.title ?: hit.commentText ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = "By: ${hit.author ?: "Unknown"} | Points: ${hit.points ?: 0}${if (host.isNotEmpty()) " | $host" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(
                modifier = Modifier.width(75.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "💬 ${hit.numComments ?: 0}",
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
