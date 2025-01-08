package com.exner.tools.activitytimercompanion.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class TVConnectionStateHolderImpl @Inject constructor() : TVConnectionStateHolder {
    private val _tvConnectionState = MutableStateFlow(TVConnectionState())
    override val tvConnectionState: StateFlow<TVConnectionState> = _tvConnectionState

    override fun updateTVConnectionState(isConnectedToTV: Boolean) {
        // atomic
        _tvConnectionState.update { current ->
            current.copy(isConnectedToTV = isConnectedToTV)
        }
    }
}