package com.example.hackernews_client.screens

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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackernews_client.api.AlgoliaHit
import com.example.hackernews_client.ui.theme.ExposedDropdownMenu
import com.example.hackernews_client.viemodels.SearchUiState
import com.example.hackernews_client.viemodels.SearchViewModel
import java.net.URL

enum class SearchSort(val label: String) {
    POPULARITY("Popularity"),
    DATE("Date")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChanged(it) },
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
                            viewModel.onSortChanged(SearchSort.entries[index])
                        },
                        modifier = Modifier.width(150.dp)
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is SearchUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Type something to search", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                is SearchUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        if (!state.message.contains("No results")) {
                            Button(
                                onClick = { viewModel.retry() },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is SearchUiState.Success -> {
                    val hits = state.hits
                    val shouldLoadMore = remember {
                        derivedStateOf {
                            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                            lastVisibleItemIndex >= hits.size - 5
                        }
                    }

                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value) {
                            viewModel.loadMore()
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(hits, key = { _, hit -> hit.objectID }) { _, hit ->
                            SearchHitItem(hit)
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
