package com.example.hackernews_client.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey

@Composable
fun AppNavigationBar(
    selectedKey: NavKey,
    destinations: List<Screen>,
    onSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        destinations.forEach { screen ->
            val topLevel = screen as? TopLevelDestination
            NavigationBarItem(
                selected = selectedKey == screen,
                onClick = {onSelected(screen)},
                icon = {
                    topLevel?.icon?.let {
                        Icon(imageVector = it, contentDescription = null)
                    }
                },
                label = {
                    topLevel?.label?.let {
                        Text(text = it)
                    }
                }
                
            )
        }
    }
}