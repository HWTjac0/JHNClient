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

        ThemeOption(
            theme = AppTheme.DEFAULT,
            selected = currentTheme == AppTheme.DEFAULT,
            onClick = { onThemeChange(AppTheme.DEFAULT) },
            label = "Default (System)"
        )

        ThemeOption(
            theme = AppTheme.HACKER_NEWS,
            selected = currentTheme == AppTheme.HACKER_NEWS,
            onClick = { onThemeChange(AppTheme.HACKER_NEWS) },
            label = "Hacker News (Orange)"
        )

        ThemeOption(
            theme = AppTheme.DEEP_BLUE,
            selected = currentTheme == AppTheme.DEEP_BLUE,
            onClick = { onThemeChange(AppTheme.DEEP_BLUE) },
            label = "Deep Blue"
        )

        ThemeOption(
            theme = AppTheme.KANAGAWA,
            selected = currentTheme == AppTheme.KANAGAWA,
            onClick = { onThemeChange(AppTheme.KANAGAWA) },
            label = "Kanagawa (Dark)"
        )
    }
}

@Composable
fun ThemeOption(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
