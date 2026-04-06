package com.example.hackernews_client.viemodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.paging.StoryPagingSource
import com.example.hackernews_client.repository.AppRepository
import com.example.hackernews_client.screens.StoryType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    private val _selectedType = MutableStateFlow(StoryType.TOP)
    val selectedType: StateFlow<StoryType> = _selectedType.asStateFlow()

    val allTags: StateFlow<List<String>> = repository.getAllTagNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val stories: Flow<PagingData<HNItem>> = _selectedType
        .flatMapLatest { type ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { StoryPagingSource(type) }
            ).flow
        }.cachedIn(viewModelScope)

    fun onTypeSelected(type: StoryType) {
        if (_selectedType.value != type) {
            _selectedType.value = type
        }
    }

    fun saveStory(story: HNItem, tags: List<String>) {
        viewModelScope.launch {
            repository.saveStory(story, tags)
        }
    }
}
