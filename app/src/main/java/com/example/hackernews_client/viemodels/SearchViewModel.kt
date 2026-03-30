package com.example.hackernews_client.viemodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackernews_client.api.AlgoliaHN
import com.example.hackernews_client.api.AlgoliaHit
import com.example.hackernews_client.screens.SearchSort
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedSort = MutableStateFlow(SearchSort.POPULARITY)
    val selectedSort: StateFlow<SearchSort> = _selectedSort.asStateFlow()

    private val _hits = MutableStateFlow<List<AlgoliaHit>>(emptyList())
    val hits: StateFlow<List<AlgoliaHit>> = _hits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 0
    private var hasMorePages = true
    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        search()
    }

    fun onSortChanged(sort: SearchSort) {
        if (_selectedSort.value != sort) {
            _selectedSort.value = sort
            search()
        }
    }

    private fun search() {
        searchJob?.cancel()
        if (_query.value.isBlank()) {
            _hits.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _isLoading.value = true
            currentPage = 0
            _hits.value = emptyList()
            try {
                val response = if (_selectedSort.value == SearchSort.POPULARITY) {
                    AlgoliaHN.service.search(query = _query.value, page = 0)
                } else {
                    AlgoliaHN.service.searchByDate(query = _query.value, page = 0)
                }
                _hits.value = response.hits
                hasMorePages = response.page < response.nbPages - 1
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value || !hasMorePages || _query.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val nextPage = currentPage + 1
            try {
                val response = if (_selectedSort.value == SearchSort.POPULARITY) {
                    AlgoliaHN.service.search(query = _query.value, page = nextPage)
                } else {
                    AlgoliaHN.service.searchByDate(query = _query.value, page = nextPage)
                }
                _hits.value += response.hits
                currentPage = nextPage
                hasMorePages = response.page < response.nbPages - 1
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Load more error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
