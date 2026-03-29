package com.example.hackernews_client.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hackernews_client.ui.theme.AppTheme
import com.example.hackernews_client.ui.theme.ExposedDropdownMenu

@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose Theme",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        ExposedDropdownMenu(
            options = AppTheme.entries.map { it.name },
            selectedIndex = AppTheme.entries.indexOf(currentTheme),
            onSelected = { index ->
                onThemeChange(AppTheme.entries[index])
            }
        )
    }
}
