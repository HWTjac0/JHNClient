package com.example.hackernews_client.screens

import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackernews_client.api.AlgoliaItem
import com.example.hackernews_client.viemodels.StoryDetailUiState
import com.example.hackernews_client.viemodels.StoryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    storyId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoryDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storyId) {
        viewModel.loadStoryDetail(storyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Story Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is StoryDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is StoryDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is StoryDetailUiState.Success -> {
                    val story = state.story
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            StoryHeader(story)
                        }
                        items(story.children) { comment ->
                            CommentItem(comment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryHeader(story: AlgoliaItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = story.title ?: "No Title", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "By: ${story.author} | Points: ${story.points}",
            style = MaterialTheme.typography.bodySmall
        )
        if (!story.text.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HtmlText(html = story.text)
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Text(
            text = "Comments",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Composable
fun CommentItem(comment: AlgoliaItem, depth: Int = 0) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = comment.author ?: "Unknown",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HtmlText(html = comment.text ?: "")
        
        comment.children.forEach { child ->
            CommentItem(comment = child, depth = depth + 1)
        }
    }
}

@Composable
fun HtmlText(html: String) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor)
            }
        },
        update = { it.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT) }
    )
}
