package com.example.hackernews_client.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.hackernews_client.api.HNItem
import com.example.hackernews_client.database.AppSetting
import com.example.hackernews_client.database.SavedStory
import com.example.hackernews_client.database.AppDbDao
import com.example.hackernews_client.database.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(private val appDbDao: AppDbDao) {

    val allTags: Flow<List<Tag>> = appDbDao.getAllTags()

    fun getSavedStoriesPaging(tags: List<String>): Flow<PagingData<SavedStory>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (tags.isEmpty()) {
                    appDbDao.getAllStoriesPaging()
                } else {
                    appDbDao.getStoriesByTags(tags)
                }
            }
        ).flow
    }

    suspend fun saveStory(item: HNItem, tags: List<String>) {
        appDbDao.saveWithTags(item, tags)
    }

    suspend fun deleteStory(storyId: Int) {
        appDbDao.deleteStory(storyId)
    }

    suspend fun deleteTag(tagName: String) {
        appDbDao.deleteTagWithReferences(tagName)
    }

    fun getAllTagNames(): Flow<List<String>> {
        return appDbDao.getAllTags().map { tags ->
            tags.map { it.name }
        }
    }

    fun getTagsForStory(storyId: Int): Flow<List<String>> {
        return appDbDao.getTagsForStory(storyId)
    }

    // Settings
    suspend fun saveSetting(key: String, value: String) {
        appDbDao.saveSetting(AppSetting(key, value))
    }

    suspend fun getSetting(key: String): String? {
        return appDbDao.getSetting(key)
    }

    fun getSettingFlow(key: String): Flow<String?> {
        return appDbDao.getSettingFlow(key)
    }
}
