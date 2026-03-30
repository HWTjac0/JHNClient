package com.example.hackernews_client.viemodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackernews_client.api.AlgoliaHN
import com.example.hackernews_client.api.AlgoliaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StoryDetailUiState {
    object Loading : StoryDetailUiState
    data class Success(val story: AlgoliaItem) : StoryDetailUiState
    data class Error(val message: String) : StoryDetailUiState
}

class StoryDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StoryDetailUiState>(StoryDetailUiState.Loading)
    val uiState: StateFlow<StoryDetailUiState> = _uiState.asStateFlow()

    fun loadStoryDetail(storyId: Int) {
        viewModelScope.launch {
            _uiState.value = StoryDetailUiState.Loading
            try {
                val story = AlgoliaHN.service.getItemWithComments(storyId)
                _uiState.value = StoryDetailUiState.Success(story)
            } catch (e: Exception) {
                Log.e("StoryDetailViewModel", "Error loading story detail", e)
                _uiState.value = StoryDetailUiState.Error("Failed to load story details")
            }
        }
    }
}
