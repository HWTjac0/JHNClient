package com.example.hackernews_client.viemodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackernews_client.api.AlgoliaItem
import com.example.hackernews_client.api.FirebaseHN
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface StoryDetailUiState {
    object Loading : StoryDetailUiState
    data class Success(val story: AlgoliaItem) : StoryDetailUiState
    data class Error(val message: String) : StoryDetailUiState
}

class StoryDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StoryDetailUiState>(StoryDetailUiState.Loading)
    val uiState: StateFlow<StoryDetailUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    fun loadStoryDetail(storyId: Int) {
        viewModelScope.launch {
            _uiState.value = StoryDetailUiState.Loading
            try {
                val story = fetchItemWithComments(storyId, 0)
                if (story != null) {
                    _uiState.value = StoryDetailUiState.Success(story)
                } else {
                    _uiState.value = StoryDetailUiState.Error("Failed to load story details")
                }
            } catch (e: Exception) {
                Log.e("StoryDetailViewModel", "Error loading story detail", e)
                _uiState.value = StoryDetailUiState.Error("Failed to load story details")
            }
        }
    }

    private suspend fun fetchItemWithComments(id: Int, depth: Int): AlgoliaItem? = coroutineScope {
        if (depth > 10) return@coroutineScope null

        val item = try {
            FirebaseHN.service.getItem(id)
        } catch (e: Exception) {
            null
        } ?: return@coroutineScope null

        val children = item.kids?.map { kidId ->
            async { fetchItemWithComments(kidId, depth + 1) }
        }?.awaitAll()?.filterNotNull() ?: emptyList()

        AlgoliaItem(
            id = item.id,
            createdAt = item.time?.let { dateFormat.format(Date(it * 1000)) } ?: "",
            author = item.by,
            title = item.title,
            url = item.url,
            text = item.text,
            points = item.score,
            parentId = item.parent,
            children = children
        )
    }
}
