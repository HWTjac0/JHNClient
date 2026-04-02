package com.example.hackernews_client.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedStory::class, Tag::class, StoryTag::class],
    version = 1,
)
abstract class SavedStoryDatabase : RoomDatabase() {
    abstract fun savedStoryDao(): SavedStoryDao

    companion object {
        const val DATABASE_NAME = "saved_stories_database"

        @Volatile
        private var INSTANCE: SavedStoryDatabase? = null

        fun getDatabase(context: Context): SavedStoryDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                                context.applicationContext,
                                SavedStoryDatabase::class.java,
                                DATABASE_NAME
                            ).fallbackToDestructiveMigration(false)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
