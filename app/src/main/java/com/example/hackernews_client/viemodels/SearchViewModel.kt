package com.example.hackernews_client.viemodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.hackernews_client.api.AlgoliaHit
import com.example.hackernews_client.paging.SearchPagingSource
import com.example.hackernews_client.screens.SearchSort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest

class SearchViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedSort = MutableStateFlow(SearchSort.POPULARITY)
    val selectedSort: StateFlow<SearchSort> = _selectedSort.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResults: Flow<PagingData<AlgoliaHit>> = combine(_query, _selectedSort) { query, sort ->
        query to sort
    }
        .debounce(500)
        .flatMapLatest { (query, sort) ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { SearchPagingSource(query, sort) }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onSortChanged(sort: SearchSort) {
        if (_selectedSort.value != sort) {
            _selectedSort.value = sort
        }
    }
}
