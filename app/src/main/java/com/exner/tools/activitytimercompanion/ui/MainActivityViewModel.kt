package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.ViewModel
import com.exner.tools.activitytimercompanion.data.preferences.ActivityTimerCompanionUserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesManager: ActivityTimerCompanionUserPreferencesManager
) : ViewModel() {

    private var _connectedToTV: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val connectedToTV: StateFlow<Boolean> = _connectedToTV

    fun updateConnectedToTV(connectedToTV: Boolean) {
        _connectedToTV.value = connectedToTV
    }

}