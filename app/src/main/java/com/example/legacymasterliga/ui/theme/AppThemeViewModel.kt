package com.example.legacymasterliga.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.ThemePreference
import com.example.legacymasterliga.feature.settings.domain.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    repository: SettingsRepository,
) : ViewModel() {
    val themePreference: StateFlow<ThemePreference> = repository.observeAppPreferences()
        .map { it.themePreference }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.DARK)
}
