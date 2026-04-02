package com.example.hackernews_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.hackernews_client.api.ApiClient
import com.example.hackernews_client.database.SavedStoryDatabase
import com.example.hackernews_client.nav.NavigationRoot
import com.example.hackernews_client.repository.SavedStoryRepository
import com.example.hackernews_client.ui.theme.AppTheme
import com.example.hackernews_client.ui.theme.Hackernews_clientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        
        val database = SavedStoryDatabase.getDatabase(this)
        val repository = SavedStoryRepository(database.savedStoryDao())
        
        enableEdgeToEdge()
        setContent {
            var currentTheme by remember { mutableStateOf(AppTheme.DEFAULT) }
            
            Hackernews_clientTheme(theme = currentTheme) {
                NavigationRoot(
                    repository = repository,
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it }
                )
            }
        }
    }
}
