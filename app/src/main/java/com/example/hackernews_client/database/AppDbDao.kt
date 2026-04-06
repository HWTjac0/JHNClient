package com.example.hackernews_client.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hackernews_client.api.HNItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDbDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: SavedStory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryTag(storyTag: StoryTag)

    @Transaction
    suspend fun saveWithTags(item: HNItem, tags: List<String>) {
        insertStory(SavedStory(item.id, item.title ?: "", item.url, item.by ?: "", item.score ?: 0))
        // Najpierw usuwamy stare tagi dla tej historii
        deleteStoryTags(item.id)
        // Potem dodajemy nowe
        tags.forEach { tag ->
            insertTag(Tag(tag))
            insertStoryTag(StoryTag(item.id, tag))
        }
    }

    @Query("DELETE FROM story_tags WHERE storyId = :storyId")
    suspend fun deleteStoryTags(storyId: Int)

    @Query("DELETE FROM saved_stories WHERE id = :storyId")
    suspend fun deleteStory(storyId: Int)

    @Query("DELETE FROM tags WHERE name = :tagName")
    suspend fun deleteTag(tagName: String)

    @Query("DELETE FROM story_tags WHERE tagName = :tagName")
    suspend fun deleteStoryTagsByTag(tagName: String)

    @Transaction
    suspend fun deleteTagWithReferences(tagName: String) {
        deleteStoryTagsByTag(tagName)
        deleteTag(tagName)
    }

    @Query("SELECT tagName FROM story_tags WHERE storyId = :storyId")
    fun getTagsForStory(storyId: Int): Flow<List<String>>

    @Query("""
        SELECT DISTINCT saved_stories.* FROM saved_stories
        JOIN story_tags ON saved_stories.id = story_tags.storyId
        WHERE story_tags.tagName IN (:tags)
        ORDER BY saved_stories.savedAt DESC
    """)
    fun getStoriesByTags(tags: List<String>): PagingSource<Int, SavedStory>

    @Query("SELECT * FROM saved_stories ORDER BY savedAt DESC")
    fun getAllStoriesPaging(): PagingSource<Int, SavedStory>

    @Query("SELECT * FROM saved_stories ORDER BY savedAt DESC")
    fun getAllStories(): Flow<List<SavedStory>>

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_stories WHERE id = :storyId)")
    fun isStorySaved(storyId: Int): Flow<Boolean>

    // Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    fun getSettingFlow(key: String): Flow<String?>
}
