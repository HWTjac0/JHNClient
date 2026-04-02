package com.example.hackernews_client.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.hackernews_client.api.AlgoliaHN
import com.example.hackernews_client.api.AlgoliaHit
import com.example.hackernews_client.screens.SearchSort

class SearchPagingSource(
    private val query: String,
    private val sort: SearchSort
) : PagingSource<Int, AlgoliaHit>() {

    override fun getRefreshKey(state: PagingState<Int, AlgoliaHit>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AlgoliaHit> {
        if (query.isBlank()) {
            return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
        }

        return try {
            val currentPage = params.key ?: 0
            val response = when (sort) {
                SearchSort.POPULARITY -> {
                    AlgoliaHN.service.search(query = query, page = currentPage)
                }
                else -> {
                    AlgoliaHN.service.searchByDate(query = query, page = currentPage)
                }
            }

            val hasMore = response.page < response.nbPages - 1

            LoadResult.Page(
                data = response.hits,
                prevKey = if (currentPage == 0) null else currentPage - 1,
                nextKey = if (hasMore) currentPage + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
