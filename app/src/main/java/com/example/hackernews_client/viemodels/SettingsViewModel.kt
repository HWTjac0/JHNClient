package com.example.hackernews_client.viemodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackernews_client.repository.AppRepository
import com.example.hackernews_client.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: AppRepository) : ViewModel() {

    val currentTheme: StateFlow<AppTheme> = repository.getSettingFlow("theme")
        .map { value ->
            AppTheme.fromName(value)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.Default)

    fun saveTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.saveSetting("theme", theme.name)
        }
    }
}
