package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditorFrontDoorViewModel @Inject constructor(
) : ViewModel() {
    private lateinit var connectionsClient: ConnectionsClient

    fun provideConnectionsClient(newConnectionsClient: ConnectionsClient) {
        connectionsClient = newConnectionsClient
    }
}