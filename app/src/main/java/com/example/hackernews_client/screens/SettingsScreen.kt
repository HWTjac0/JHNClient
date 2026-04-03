package com.example.hackernews_client.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackernews_client.R
import com.example.hackernews_client.ui.theme.AppTheme
import com.example.hackernews_client.ui.theme.ExposedDropdownMenu
import com.example.hackernews_client.viemodels.SettingsViewModel

@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.navsettings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenu(
            options = AppTheme.entries.map { it.name },
            selectedIndex = AppTheme.entries.indexOf(currentTheme),
            onSelected = { index ->
                val selectedTheme = AppTheme.entries[index]
                viewModel.saveTheme(selectedTheme)
                onThemeChange(selectedTheme)
            }
        )
    }
}
