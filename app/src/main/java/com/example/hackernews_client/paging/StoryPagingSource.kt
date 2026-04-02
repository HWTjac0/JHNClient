package com.example.hackernews_client.paging

import android.util.Log.e
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.hackernews_client.api.FirebaseHN
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.screens.StoryType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class StoryPagingSource(
    private val type: StoryType
) : PagingSource<Int, HNItem>() {

    private var cachedIds: List<Int>? = null

    override fun getRefreshKey(state: PagingState<Int, HNItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HNItem> {
        return try {
            val currentPage = params.key ?: 0
            val allIds = cachedIds ?: when(type) {
                StoryType.TOP -> FirebaseHN.service.getTopStories()
                StoryType.NEW -> FirebaseHN.service.getNewStories()
                StoryType.JOBS -> FirebaseHN.service.getJobStories()
            }.also { cachedIds = it }

            val start = currentPage * params.loadSize
            val end = minOf(start + params.loadSize, allIds.size)

            if(start >= allIds.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }

            val pageIds = allIds.subList(start, end)
            val items = coroutineScope {
                pageIds.map { id ->
                    async {
                        try {
                            FirebaseHN.service.getItem(id)}  catch (e: Exception) {null}
                    }
                }.awaitAll().filterNotNull()
            }
            LoadResult.Page(
                data = items,
                prevKey = if (currentPage == 0) null else currentPage - 1,
                nextKey = if (end >= allIds.size) null else currentPage + 1
            )
        } catch (e: Exception){
            LoadResult.Error(e)
            }
    }
}