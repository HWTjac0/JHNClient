package com.example.hackernews_client.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.hackernews_client.screens.MainScreen
import com.example.hackernews_client.screens.SavedScreen
import com.example.hackernews_client.screens.SearchScreen
import com.example.hackernews_client.screens.SettingsScreen
import com.example.hackernews_client.ui.theme.AppTheme
import kotlinx.serialization.Serializable

interface TopLevelDestination {
    val label: String
    val icon: ImageVector
}
@Serializable
sealed interface  Screen : NavKey {
    @Serializable data object Main : Screen, TopLevelDestination {
        override val label: String
            get() = "Stories"
        override val icon: ImageVector
            get() = Icons.Default.Home
    }
    @Serializable data object Search : Screen, TopLevelDestination {
        override val label: String
            get() = "Search"
        override val icon: ImageVector
            get() = Icons.Default.Search
    }
    @Serializable data object Saved : Screen, TopLevelDestination {
        override val label: String
            get() = "Saved"
        override val icon: ImageVector
            get() = Icons.Default.Favorite
    }
    @Serializable data object Settings : Screen, TopLevelDestination {
        override val label: String
            get() = "Settings"
        override val icon: ImageVector
            get() = Icons.Default.Settings
    }
    @Serializable data class StoryDetail(val itemId: Int) : Screen
    @Serializable data class WebView(val url: String) : Screen
}

@Composable
fun NavigationRoot(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Screen.Main)
    val destinations = listOf<Screen>(Screen.Main, Screen.Search, Screen.Saved, Screen.Settings)
    Scaffold(
        modifier = modifier,
        bottomBar = {
            AppNavigationBar(
                selectedKey = backStack.last(),
                destinations = destinations,
                onSelected = {selectedScreen ->
                    backStack.add(selectedScreen)
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
            backStack = backStack,
            onBack = {backStack.removeLastOrNull()},
            entryProvider = entryProvider {
                entry<Screen.Main> { MainScreen() }
                entry<Screen.Search> { SearchScreen() }
                entry<Screen.Saved> { SavedScreen() }
                entry<Screen.Settings> { 
                    SettingsScreen(
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange
                    ) 
                }
            },
            transitionSpec = {
                // Slide in from right when navigating forward
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },

        )
    }

}
