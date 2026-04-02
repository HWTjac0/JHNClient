package com.example.hackernews_client.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "saved_stories")
data class SavedStory (
    @PrimaryKey val id: Int,
    val title: String,
    val url: String?,
    val by: String,
    val score: Int,
    val savedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "tags")
data class Tag(@PrimaryKey val name: String)

@Entity(
    tableName = "story_tags",
    primaryKeys = ["storyId", "tagName"],
    indices = [Index("tagName")]
)
data class StoryTag(val storyId: Int, val tagName: String)