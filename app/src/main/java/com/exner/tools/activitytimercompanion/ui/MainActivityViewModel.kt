package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exner.tools.activitytimercompanion.data.preferences.ActivityTimerCompanionUserPreferencesManager
import com.exner.tools.activitytimercompanion.network.TimerEndpoint
import com.exner.tools.activitytimercompanion.state.TVConnectionStateHolder
import com.exner.tools.activitytimercompanion.ui.destinations.wrappers.ConnectionStateConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesManager: ActivityTimerCompanionUserPreferencesManager,
    val tvConnectionStateHolder: TVConnectionStateHolder
) : ViewModel() {

    fun updateConnectedToTV(connectedToTV: Boolean) {
        tvConnectionStateHolder.updateTVConnection(isConnectedToTV = connectedToTV)
    }

    private val _connectionUIState = MutableStateFlow(ConnectionUIState())
    val connectionUIState: StateFlow<ConnectionUIState> = _connectionUIState
    fun updateConnectionUIState(newState: ConnectionStateConstants) {
        _connectionUIState.value = ConnectionUIState(newState)
    }
    
    private val _discoveredDevices: MutableLiveData<List<TimerEndpoint>> = MutableLiveData<List<TimerEndpoint>>()
    val discoveredDevices: LiveData<List<TimerEndpoint>> = _discoveredDevices
    fun addDiscoveredDevice(discoveredDevice: TimerEndpoint) {
        val currentList = _discoveredDevices.value
        if (currentList != null) {
            _discoveredDevices.value = currentList.toMutableList()
        } else {
            _discoveredDevices.value = mutableListOf(discoveredDevice)
        }
    }
    fun removeDiscoveredDevice(endpointId: String) {
        val currentList = _discoveredDevices.value
        if (!currentList.isNullOrEmpty()) {
            val tempList = currentList.toMutableList()
            tempList.removeIf { it.endpointId == endpointId }
            _discoveredDevices.value = tempList
        }
    }
    fun resetDiscoveredDevices() {
        _discoveredDevices.value = listOf<TimerEndpoint>()
    }
}
