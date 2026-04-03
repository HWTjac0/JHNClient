package com.example.hackernews_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.hackernews_client.api.ApiClient
import com.example.hackernews_client.database.SavedStoryDatabase
import com.example.hackernews_client.nav.NavigationRoot
import com.example.hackernews_client.repository.SavedStoryRepository
import com.example.hackernews_client.ui.theme.AppTheme
import com.example.hackernews_client.ui.theme.Hackernews_clientTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        
        val database = SavedStoryDatabase.getDatabase(this)
        val repository = SavedStoryRepository(database.savedStoryDao())
        
        enableEdgeToEdge()
        setContent {
            val savedThemeName by repository.getSettingFlow("theme").collectAsState(initial = null)
            val currentTheme = savedThemeName?.let { 
                try { AppTheme.valueOf(it) } catch (e: Exception) { AppTheme.DEFAULT }
            } ?: AppTheme.DEFAULT
            
            val scope = rememberCoroutineScope()
            
            Hackernews_clientTheme(theme = currentTheme) {
                NavigationRoot(
                    repository = repository,
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme ->
                        scope.launch {
                            repository.saveSetting("theme", newTheme.name)
                        }
                    }
                )
            }
        }
    }
}
