package com.example.hackernews_client.viemodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackernews_client.api.AlgoliaItem
import com.example.hackernews_client.api.FirebaseHN
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.paging.CommentPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface StoryDetailUiState {
    object Loading : StoryDetailUiState
    data class Success(val story: HNItem) : StoryDetailUiState
    data class Error(val message: String) : StoryDetailUiState
}

class StoryDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StoryDetailUiState>(StoryDetailUiState.Loading)
    val uiState: StateFlow<StoryDetailUiState> = _uiState.asStateFlow()

    private val _commentsFlow = MutableStateFlow<Flow<PagingData<HNItem>>>(emptyFlow())
    val commentsFlow: StateFlow<Flow<PagingData<HNItem>>> = _commentsFlow.asStateFlow()

    fun loadStoryDetail(storyId: Int) {
        viewModelScope.launch {
            _uiState.value = StoryDetailUiState.Loading
            try {
                val story = FirebaseHN.service.getItem(storyId)
                if (story != null) {
                    _uiState.value = StoryDetailUiState.Success(story)
                    
                    if (!story.kids.isNullOrEmpty()) {
                        _commentsFlow.value = Pager(
                            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                            pagingSourceFactory = { CommentPagingSource(story.kids) }
                        ).flow.cachedIn(viewModelScope)
                    } else {
                        _commentsFlow.value = emptyFlow()
                    }
                } else {
                    _uiState.value = StoryDetailUiState.Error("Failed to load story details")
                }
            } catch (e: Exception) {
                Log.e("StoryDetailViewModel", "Error loading story detail", e)
                _uiState.value = StoryDetailUiState.Error("Failed to load story details")
            }
        }
    }
}
