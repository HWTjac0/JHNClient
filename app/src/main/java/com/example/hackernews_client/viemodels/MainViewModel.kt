package com.example.hackernews_client.viemodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackernews_client.api.FirebaseHN
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.screens.StoryType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _selectedType = MutableStateFlow(StoryType.TOP)
    val selectedType: StateFlow<StoryType> = _selectedType.asStateFlow()

    private val _stories = MutableStateFlow<List<HNItem>>(emptyList())
    val stories: StateFlow<List<HNItem>> = _stories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private var allStoryIds = emptyList<Int>()
    private val pageSize = 20

    init {
        loadInitialStories()
    }

    fun onTypeSelected(type: StoryType) {
        if (_selectedType.value != type) {
            _selectedType.value = type
            loadInitialStories()
        }
    }

    private fun loadInitialStories() {
        viewModelScope.launch {
            _isInitialLoading.value = true
            _stories.value = emptyList()
            allStoryIds = emptyList()
            try {
                allStoryIds = when (_selectedType.value) {
                    StoryType.TOP -> FirebaseHN.service.getTopStories()
                    StoryType.NEW -> FirebaseHN.service.getNewStories()
                    StoryType.JOBS -> FirebaseHN.service.getJobStories()
                }
                val initialIds = allStoryIds.take(pageSize)
                val loadedStories = initialIds.map { id ->
                    async {
                        try { FirebaseHN.service.getItem(id) } catch (e: Exception) { null }
                    }
                }.awaitAll().filterNotNull()
                _stories.value = loadedStories
            } catch (e: Exception) {
                Log.e("MainViewModel", "Initial load error", e)
            } finally {
                _isInitialLoading.value = false
            }
        }
    }

    fun loadMoreStories() {
        if (_isLoading.value || _stories.value.size >= allStoryIds.size) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentSize = _stories.value.size
                val nextIds = allStoryIds.drop(currentSize).take(pageSize)
                val loadedStories = nextIds.map { id ->
                    async {
                        try { FirebaseHN.service.getItem(id) } catch (e: Exception) { null }
                    }
                }.awaitAll().filterNotNull()
                _stories.value += loadedStories
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading more stories", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
