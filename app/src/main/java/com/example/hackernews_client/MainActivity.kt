package com.example.hackernews_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.hackernews_client.nav.NavigationRoot
import com.example.hackernews_client.ui.theme.AppTheme
import com.example.hackernews_client.ui.theme.Hackernews_clientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentTheme by remember { mutableStateOf(AppTheme.DEFAULT) }
            
            Hackernews_clientTheme(theme = currentTheme) {
                NavigationRoot(
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it }
                )
            }
        }
    }
}
