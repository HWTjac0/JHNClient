package com.example.hackernews_client.screens

import android.text.Html
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackernews_client.R
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

    when (val state = uiState) {
        is StoryDetailUiState.Loading -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.loading)) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        is StoryDetailUiState.Error -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.error)) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)) {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        is StoryDetailUiState.Success -> {
            val story = state.story
            if (!story.url.isNullOrBlank()) {
                StoryWithWebView(story = story, onBack = onBack)
            } else {
                StoryOnlyComments(story = story, onBack = onBack)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryWithWebView(story: AlgoliaItem, onBack: () -> Unit) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var canGoBack by remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState()

    BackHandler {
        if (canGoBack) {
            webView?.goBack()
        } else {
            onBack()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        topBar = {
            TopAppBar(
                title = { Text(text = story.title ?: stringResource(R.string.story), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (canGoBack) webView?.goBack() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        sheetContent = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                item {
                    StoryHeader(story, showDivider = false)
                }
                item {
                    Text(
                        text = stringResource(R.string.comments),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(story.children) { comment ->
                    CommentThread(comment, depth = 0)
                }
            }
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            canGoBack = view?.canGoBack() ?: false
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl(story.url!!)
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryOnlyComments(story: AlgoliaItem, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.story_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            item {
                StoryHeader(story)
            }
            item {
                Text(
                    text = stringResource(R.string.comments),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(story.children) { comment ->
                CommentThread(comment, depth = 0)
            }
        }
    }
}

@Composable
fun StoryHeader(story: AlgoliaItem, showDivider: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = story.title ?: stringResource(R.string.no_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.by_points, story.author ?: stringResource(R.string.unknown), story.points ?: 0),
            style = MaterialTheme.typography.bodySmall
        )
        if (!story.text.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HtmlText(html = story.text)
        }
        if (showDivider) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
        }
    }
}

@Composable
fun CommentThread(comment: AlgoliaItem, depth: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommentItem(comment = comment, depth = depth)
        comment.children.forEach { child ->
            CommentThread(comment = child, depth = depth + 1)
        }
    }
}

@Composable
fun CommentItem(comment: AlgoliaItem, depth: Int) {
    val indent = (depth * 12).dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        if (depth > 0) {
            Box(
                modifier = Modifier
                    .width(indent)
                    .padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.author ?: stringResource(R.string.unknown),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            HtmlText(html = comment.text ?: stringResource(R.string.deleted))
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
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
                textSize = 14f
            }
        },
        update = { it.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT) }
    )
}
