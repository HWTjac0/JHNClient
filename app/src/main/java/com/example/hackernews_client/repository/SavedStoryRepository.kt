package com.example.hackernews_client.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.database.SavedStory
import com.example.hackernews_client.database.SavedStoryDao
import com.example.hackernews_client.database.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedStoryRepository(private val savedStoryDao: SavedStoryDao) {

    val allTags: Flow<List<Tag>> = savedStoryDao.getAllTags()

    fun getSavedStoriesPaging(tags: List<String>): Flow<PagingData<SavedStory>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (tags.isEmpty()) {
                    savedStoryDao.getAllStoriesPaging()
                } else {
                    savedStoryDao.getStoriesByTags(tags)
                }
            }
        ).flow
    }

    suspend fun saveStory(item: HNItem, tags: List<String>) {
        savedStoryDao.saveWithTags(item, tags)
    }

    suspend fun deleteStory(storyId: Int) {
        savedStoryDao.deleteStory(storyId)
    }

    fun getAllTagNames(): Flow<List<String>> {
        return savedStoryDao.getAllTags().map { tags ->
            tags.map { it.name }
        }
    }
}
