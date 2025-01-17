package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.ViewModel
import com.exner.tools.activitytimercompanion.state.TVConnectionState
import com.exner.tools.activitytimercompanion.state.TVConnectionStateHolder
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditorFrontDoorViewModel @Inject constructor(
    val tvConnectionStateHolder: TVConnectionStateHolder
) : ViewModel() {

    private lateinit var connectionsClient: ConnectionsClient

    fun provideConnectionsClient(newConnectionsClient: ConnectionsClient) {
        connectionsClient = newConnectionsClient
    }

    fun updateTvConnectionState(newState: Boolean) {
        tvConnectionStateHolder.updateTVConnection(newState)
    }
}