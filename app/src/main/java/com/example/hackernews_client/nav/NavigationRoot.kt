package com.example.hackernews_client.nav

import androidx.annotation.StringRes
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.hackernews_client.R
import com.example.hackernews_client.repository.AppRepository
import com.example.hackernews_client.screens.MainScreen
import com.example.hackernews_client.screens.SavedScreen
import com.example.hackernews_client.screens.SearchScreen
import com.example.hackernews_client.screens.SettingsScreen
import com.example.hackernews_client.screens.StoryDetailScreen
import com.example.hackernews_client.ui.theme.AppTheme
import com.example.hackernews_client.viemodels.MainViewModel
import com.example.hackernews_client.viemodels.SavedViewModel
import com.example.hackernews_client.viemodels.SearchViewModel
import com.example.hackernews_client.viemodels.SettingsViewModel
import kotlinx.serialization.Serializable

interface TopLevelDestination {
    @get:StringRes
    val label: Int
    val icon: ImageVector
}
@Serializable
sealed interface  Screen : NavKey {
    @Serializable data object Main : Screen, TopLevelDestination {
        override val label: Int
            get() = R.string.nav_stories
        override val icon: ImageVector
            get() = Icons.Default.Home
    }
    @Serializable data object Search : Screen, TopLevelDestination {
        override val label: Int
            get() = R.string.nav_search
        override val icon: ImageVector
            get() = Icons.Default.Search
    }
    @Serializable data object Saved : Screen, TopLevelDestination {
        override val label: Int
            get() = R.string.nav_saved
        override val icon: ImageVector
            get() = Icons.Default.Favorite
    }
    @Serializable data object Settings : Screen, TopLevelDestination {
        override val label: Int
            get() = R.string.navsettings
        override val icon: ImageVector
            get() = Icons.Default.Settings
    }
    @Serializable data class StoryDetail(val itemId: Int) : Screen
}

@Composable
fun NavigationRoot(
    repository: AppRepository,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Screen.Main)
    val destinations = listOf<Screen>(Screen.Main, Screen.Search, Screen.Saved, Screen.Settings)

    val showBottomBar = backStack.last() in destinations
    var previousScreen by remember { mutableStateOf(backStack.last()) }
    val currentScreen = backStack.last()

    val viewModelFactory = remember(repository) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repository) as T
                    modelClass.isAssignableFrom(SavedViewModel::class.java) -> SavedViewModel(repository) as T
                    modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel() as T
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if(!showBottomBar) {
                return@Scaffold
            }
            AppNavigationBar(
                selectedKey = backStack.last(),
                destinations = destinations,
                onSelected = {selectedScreen ->
                    previousScreen = currentScreen
                    backStack.clear()
                    backStack.add(selectedScreen)
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            backStack = backStack,
            onBack = {backStack.removeLastOrNull()},
            entryProvider = entryProvider {
                entry<Screen.Main> {
                    MainScreen(
                        viewModel = viewModel<MainViewModel>(factory = viewModelFactory),
                        onStoryClick = { storyId ->
                            backStack.add(Screen.StoryDetail(storyId))
                        }
                    )
                }
                entry<Screen.Search> {
                    SearchScreen(
                        viewModel = viewModel<SearchViewModel>(),
                        onStoryClick = { storyId ->
                            backStack.add(Screen.StoryDetail(storyId))
                        }
                    )
                }
                entry<Screen.Saved> { 
                    SavedScreen(
                        viewModel = viewModel<SavedViewModel>(factory = viewModelFactory),
                        onStoryClick = { storyId ->
                            backStack.add(Screen.StoryDetail(storyId))
                        }
                    ) 
                }
                entry<Screen.Settings> { 
                    SettingsScreen(
                        viewModel = viewModel<SettingsViewModel>(factory = viewModelFactory),
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange
                    ) 
                }
                entry<Screen.StoryDetail> {
                    StoryDetailScreen(
                        storyId = it.itemId,
                        onBack = {backStack.removeLastOrNull()},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            transitionSpec = {
                if (
                    currentScreen in destinations &&
                    destinations.indexOf(previousScreen) > destinations.indexOf(backStack.last())
                    ) {
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                } else {
                    slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
                }
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
