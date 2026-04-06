package com.example.hackernews_client.screens

import android.text.Html
import android.util.Log
import android.view.textclassifier.TextLinks
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackernews_client.R
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.hackernews_client.api.FirebaseHN
import com.example.hackernews_client.api.HNItem
import kotlinx.coroutines.flow.Flow
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
            val commentsFlow by viewModel.commentsFlow.collectAsState()
            val lazyComments = commentsFlow.collectAsLazyPagingItems()

            if (!story.url.isNullOrBlank()) {
                StoryWithWebView(story = story, lazyComments = lazyComments, onBack = onBack)
            } else {
                StoryOnlyComments(story = story, lazyComments = lazyComments, onBack = onBack)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryWithWebView(
    story: HNItem,
    lazyComments: LazyPagingItems<HNItem>,
    onBack: () -> Unit
) {
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
                items(lazyComments.itemCount) { index ->
                    lazyComments[index]?.let { comment ->
                        CommentThread(comment, depth = 0)
                    }
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
fun StoryOnlyComments(
    story: HNItem,
    lazyComments: LazyPagingItems<HNItem>,
    onBack: () -> Unit
) {
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
            items(lazyComments.itemCount) { index ->
                lazyComments[index]?.let { comment ->
                    CommentThread(comment, depth = 0)
                }
            }
        }
    }
}

@Composable
fun StoryHeader(story: HNItem, showDivider: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = story.title ?: stringResource(R.string.no_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.by_points, story.by ?: stringResource(R.string.unknown), story.score ?: 0),
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
fun CommentThread(comment: HNItem, depth: Int) {
    var expanded by remember(comment.id) { mutableStateOf(depth < 2) }
    var children by remember { mutableStateOf<List<HNItem>>(emptyList()) }
    var loadingChildren by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded && children.isEmpty() && !comment.kids.isNullOrEmpty()) {
            loadingChildren = true
            try {
                children = comment.kids.mapNotNull { id ->
                    try {
                        FirebaseHN.service.getItem(id)
                    } catch (e: Exception) {
                        null
                    }
                }.filter { !it.deleted && !it.dead }
            } catch (e: Exception) {
            } finally {
                loadingChildren = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        CommentItem(
            comment = comment,
            depth = depth,
            hasChildren = !comment.kids.isNullOrEmpty(),
            isExpanded = expanded,
            onToggleExpand = { expanded = !expanded }
        )
        if (expanded) {
            if (loadingChildren) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(start = (depth * 12 + 24).dp, top = 8.dp, bottom = 8.dp)
                        .height(20.dp)
                        .width(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                children.forEach { child ->
                    CommentThread(comment = child, depth = depth + 1)
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: HNItem,
    depth: Int,
    hasChildren: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = hasChildren) { onToggleExpand() }
            ) {
                Text(
                    text = comment.by ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (hasChildren) {
                    Text(
                        text = if (isExpanded) "[-]" else "[+]",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
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
    val content = AnnotatedString.fromHtml(
        html,
        linkStyles = TextLinkStyles(
            style = SpanStyle(
                textDecoration = TextDecoration.Underline,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
        )
    )
    Text(
        content,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}
