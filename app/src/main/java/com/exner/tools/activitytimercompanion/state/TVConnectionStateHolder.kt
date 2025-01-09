package com.exner.tools.activitytimercompanion.state

import kotlinx.coroutines.flow.StateFlow

interface TVConnectionStateHolder {
    val tvConnectionState: StateFlow<TVConnectionState>
    fun updateTVConnection(isConnectedToTV: Boolean)
}