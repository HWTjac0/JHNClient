package com.example.hackernews_client.viemodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.database.SavedStory
import com.example.hackernews_client.database.Tag
import com.example.hackernews_client.repository.SavedStoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(private val repository: SavedStoryRepository) : ViewModel() {

    val allTags: StateFlow<List<Tag>> = repository.allTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTagNames: StateFlow<List<String>> = repository.getAllTagNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedStories: Flow<PagingData<SavedStory>> = _selectedTags
        .flatMapLatest { tags ->
            repository.getSavedStoriesPaging(tags.toList())
        }
        .cachedIn(viewModelScope)

    fun toggleTag(tagName: String) {
        val current = _selectedTags.value
        _selectedTags.value = if (current.contains(tagName)) {
            current - tagName
        } else {
            current + tagName
        }
    }

    fun deleteStory(storyId: Int) {
        viewModelScope.launch {
            repository.deleteStory(storyId)
        }
    }

    fun deleteTag(name: String) {
        viewModelScope.launch {
            repository.deleteTag(name)
            if (_selectedTags.value.contains(name)) {
                _selectedTags.value -= name
            }
        }
    }

    fun getTagsForStory(storyId: Int): Flow<List<String>> {
        return repository.getTagsForStory(storyId)
    }

    fun updateStoryTags(story: SavedStory, tags: List<String>) {
        viewModelScope.launch {
            val hnItem = HNItem(
                id = story.id,
                type = "story",
                title = story.title,
                url = story.url ?: "",
                by = story.by,
                score = story.score
            )
            repository.saveStory(hnItem, tags)
        }
    }
}
