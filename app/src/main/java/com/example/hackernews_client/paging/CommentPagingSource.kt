package com.example.hackernews_client.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.hackernews_client.api.FirebaseHN
import com.example.hackernews_client.api.HNItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CommentPagingSource(
    private val commentIds: List<Int>
) : PagingSource<Int, HNItem>() {

    override fun getRefreshKey(state: PagingState<Int, HNItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HNItem> {
        return try {
            val currentPage = params.key ?: 0
            val start = currentPage * params.loadSize
            val end = minOf(start + params.loadSize, commentIds.size)

            if (start >= commentIds.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }

            val pageIds = commentIds.subList(start, end)
            val items = coroutineScope {
                pageIds.map { id ->
                    async {
                        try {
                            FirebaseHN.service.getItem(id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull().filter { !it.deleted && !it.dead }
            }

            LoadResult.Page(
                data = items,
                prevKey = if (currentPage == 0) null else currentPage - 1,
                nextKey = if (end >= commentIds.size) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
