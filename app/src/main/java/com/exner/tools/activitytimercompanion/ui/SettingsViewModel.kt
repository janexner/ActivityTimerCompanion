package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exner.tools.activitytimercompanion.data.preferences.ActivityTimerCompanionUserPreferencesManager
import com.exner.tools.activitytimercompanion.ui.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesManager: ActivityTimerCompanionUserPreferencesManager
) : ViewModel() {

    val userSelectedTheme: StateFlow<Theme> = userPreferencesManager.theme().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        Theme.Auto
    )
    val showSimpleDisplay: StateFlow<Boolean> = userPreferencesManager.showSimpleDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        true
    )
    val chainToSameCategoryOnly: StateFlow<Boolean> = userPreferencesManager.chainToSameCategoryOnly().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        false
    )

    fun updateUserSelectedTheme(newTheme: Theme) {
        viewModelScope.launch {
            userPreferencesManager.setTheme(newTheme)
        }
    }

    fun updateShowSimpleDisplay(newSimpleDisplay: Boolean) {
        viewModelScope.launch {
            userPreferencesManager.setShowSimpleDisplay(newSimpleDisplay)
        }
    }

    fun updateChainToSameCategoryOnly(newChainToSameOnly: Boolean) {
        viewModelScope.launch {
            userPreferencesManager.setChainToSameCategoryOnly(newChainToSameOnly)
        }
    }
}