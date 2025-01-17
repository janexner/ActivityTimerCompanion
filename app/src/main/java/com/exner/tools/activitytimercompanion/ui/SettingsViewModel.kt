package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exner.tools.activitytimercompanion.data.preferences.ActivityTimerCompanionUserPreferencesManager
import com.exner.tools.activitytimercompanion.state.ThemeStateHolder
import com.exner.tools.activitytimercompanion.ui.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesManager: ActivityTimerCompanionUserPreferencesManager,
    private val themeStateHolder: ThemeStateHolder
) : ViewModel() {

    val userSelectedTheme: StateFlow<Theme> = userPreferencesManager.theme().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        Theme.Auto
    )

    fun updateUserSelectedTheme(newTheme: Theme) {
        viewModelScope.launch {
            userPreferencesManager.setTheme(newTheme)
        }
        themeStateHolder.updateTheme(newTheme)
    }
}