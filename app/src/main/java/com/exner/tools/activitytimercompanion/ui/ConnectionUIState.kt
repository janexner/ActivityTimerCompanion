package com.exner.tools.activitytimercompanion.ui

import com.exner.tools.activitytimercompanion.ui.destinations.wrappers.ConnectionStateConstants

data class ConnectionUIState(
    val currentState: ConnectionStateConstants = ConnectionStateConstants.IDLE
)
