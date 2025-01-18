package com.exner.tools.activitytimercompanion.ui.destinations.wrappers

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exner.tools.activitytimercompanion.network.EndpointConnectionInformation
import com.exner.tools.activitytimercompanion.network.TimerEndpoint
import com.exner.tools.activitytimercompanion.ui.DefaultSpacer
import com.exner.tools.activitytimercompanion.ui.MainActivityViewModel
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.wrapper.DestinationWrapper

enum class ConnectionStateConstants {
    IDLE, STARTING_DISCOVERY, DISCOVERY_STARTED, PARTNER_CHOSEN, AUTHENTICATION_REQUESTED, AUTHENTICATION_OK, AUTHENTICATION_DENIED, CONNECTING, CONNECTION_ESTABLISHED, CONNECTION_DENIED, ERROR, DISCONNECTED
}

private val STRATEGY = Strategy.P2P_POINT_TO_POINT
private const val SERVICE_ID_COMPANION = "com.exner.tools.ActivityTimer.Companion"
private const val USER_NAME = "Activity Timer Companion"
private const val TAG = "ECW"

object EstablishConnectionWrapper : DestinationWrapper {

    @Composable
    override fun <T> DestinationScope<T>.Wrap(screenContent: @Composable () -> Unit) {

        val mainActivityViewModel = hiltViewModel<MainActivityViewModel>()
        val tvConnectionState = mainActivityViewModel.tvConnectionStateHolder.tvConnectionState.collectAsState()

        val currentState = mainActivityViewModel.connectionUIState.collectAsState()

        // all the UI needed during connecting
        //
        // we're going to go straight into discover w/o asking for it
        val discoveredEndpoints: List<TimerEndpoint> by mainActivityViewModel.discoveredDevices.observeAsState(listOf())
        val pendingEndpoints: MutableMap<String, TimerEndpoint> = remember { mutableMapOf() }
        val establishedEndpoints: MutableMap<String, TimerEndpoint> = remember { mutableMapOf() }

        val openAuthenticationDialog = remember { mutableStateOf(false) }

        // for the actual discovering
        val context = LocalContext.current
        val connectionsClient = Nearby.getConnectionsClient(context)

        val connectionInformation = remember { mutableStateOf(EndpointConnectionInformation()) }

        val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
                Log.d(TAG, "On Endpoint Found... $endpointId / ${endpointInfo.endpointName}")
                val endpoint = TimerEndpoint(endpointId, endpointInfo.endpointName)
                mainActivityViewModel.addDiscoveredDevice(endpoint)
            }

            override fun onEndpointLost(endpointId: String) {
                Log.d(TAG, "On Endpoint Lost... $endpointId")
                mainActivityViewModel.removeDiscoveredDevice(endpointId)
            }
        }

        val timerLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(
                endpointId: String, connectionInfo: ConnectionInfo
            ) {
                Log.d(
                    TAG,
                    "onConnectionInitiated ${connectionInfo.endpointName} / ${connectionInfo.authenticationDigits}"
                )
                // authenticate
                connectionInformation.value = EndpointConnectionInformation(
                    endpointId = endpointId,
                    endpointName = connectionInfo.endpointName,
                    authenticationDigits = connectionInfo.authenticationDigits
                )
                // now move to auth requested
                mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.AUTHENTICATION_REQUESTED)
            }

            override fun onConnectionResult(
                endpointId: String, connectionResolution: ConnectionResolution
            ) {
                Log.d(
                    TAG, "onConnectionResult $endpointId: $connectionResolution"
                )
                if (!connectionResolution.status.isSuccess) {
                    mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.AUTHENTICATION_DENIED)
                } else {
                    // this worked!
                    val newEndpoint = pendingEndpoints.remove(endpointId)
                    establishedEndpoints[endpointId] = newEndpoint!!
                    mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.CONNECTION_ESTABLISHED)
                }
            }

            override fun onDisconnected(endpointId: String) {
                establishedEndpoints.remove(endpointId)
                mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.DISCONNECTED)
            }
        }

        if (currentState.value.currentState == ConnectionStateConstants.IDLE) {
            val discoveryOptions =
                DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
            // reset, so to speak
            connectionsClient.stopAllEndpoints()
            connectionsClient.stopDiscovery()
            // now start discovery
            Log.d(TAG, "Starting discovery... $STRATEGY / $SERVICE_ID_COMPANION")
            connectionsClient.startDiscovery(
                SERVICE_ID_COMPANION, endpointDiscoveryCallback, discoveryOptions
            ).addOnSuccessListener { _ ->
                Log.d(TAG, "Success! Discovery started")
                mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.DISCOVERY_STARTED)
            }.addOnFailureListener { e: Exception? ->
                val errorMessage = "Issue starting discovery" + if (e != null) {
                    ": ${e.message}"
                } else {
                    ""
                }
                Log.d(TAG, errorMessage)
                mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.ERROR)
            }
            mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.STARTING_DISCOVERY)
        }

        val payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                Log.d(TAG, "Payload received ${payload.id}")
                if (payload.type == Payload.Type.BYTES) {
                    Log.d(TAG, "received ${payload.type}")
                } else {
                    Log.d(TAG, "Payload received from $endpointId but wrong type: $payload")
                }
            }

            override fun onPayloadTransferUpdate(
                endpointId: String, update: PayloadTransferUpdate
            ) {
                Log.d(TAG, "Payload Transfer Update: ${update.status}")
                when (update.status) {
                    PayloadTransferUpdate.Status.CANCELED -> {
                        Log.d(TAG, "Transfer cancelled")
                    }

                    PayloadTransferUpdate.Status.FAILURE -> {
                        Log.d(TAG, "Transfer failed")
                    }

                    PayloadTransferUpdate.Status.IN_PROGRESS -> {
                        Log.d(TAG, "Transfer in progress")
                    }

                    PayloadTransferUpdate.Status.SUCCESS -> {
                        Log.d(TAG, "Transfer successful")
                    }
                }
            }
        }

        if (tvConnectionState.value.isConnectedToTV) {
            screenContent()
        } else {
            Scaffold(content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    when (currentState.value.currentState) {
                        ConnectionStateConstants.IDLE,
                        ConnectionStateConstants.STARTING_DISCOVERY -> {
                            Text(text = "Looking for a TV now...")
                            DefaultSpacer()
                            Text(text = "Make sure the Activity Timer app is running on your TV!")
                        }

                        ConnectionStateConstants.DISCOVERY_STARTED -> {
                            Text(text = "Looking for a TV running Activity Timer...")
                            if (discoveredEndpoints.isNotEmpty()) {
                                DefaultSpacer()
                                Text(text = "So far, ${discoveredEndpoints.size} TV(s) have been found. Tap one to connect.")
                            }
                            DefaultSpacer()
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                items(items = discoveredEndpoints,
                                    key = { it.endpointId }) { endpoint ->
                                    OutlinedCard(
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 2.dp
                                        ),
                                        modifier = Modifier.fillMaxSize(),
                                        border = BorderStroke(
                                            1.dp, MaterialTheme.colorScheme.onSurface
                                        ),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Box(modifier = Modifier
                                                .padding(PaddingValues(8.dp))
                                                .clickable {
                                                    connectionsClient.stopDiscovery()
                                                    mainActivityViewModel.removeDiscoveredDevice(endpoint.endpointId)
                                                    pendingEndpoints[endpoint.endpointId] = endpoint
                                                    connectionsClient.requestConnection(
                                                        USER_NAME,
                                                        endpoint.endpointId,
                                                        timerLifecycleCallback
                                                    )
                                                    mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.PARTNER_CHOSEN)
                                                }) {
                                                Text(text = "Activity Timer for TV ID: ${endpoint.endpointId}")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ConnectionStateConstants.PARTNER_CHOSEN -> {
                            Text(text = "Partner chosen, now trying to connect...")
                        }

                        ConnectionStateConstants.CONNECTING -> {
                            Text(text = "Connecting to partner...")
                        }

                        ConnectionStateConstants.AUTHENTICATION_REQUESTED -> {
                            openAuthenticationDialog.value = true
                            val dismissCallback = {
                                openAuthenticationDialog.value = false
                                mainActivityViewModel.updateConnectedToTV(connectedToTV = false)
                            }
                            if (openAuthenticationDialog.value) {
                                AlertDialog(title = { Text(text = "Accept connection to " + connectionInformation.value.endpointName) },
                                    text = { Text(text = "Confirm the code matches on both devices: " + connectionInformation.value.authenticationDigits) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Alert"
                                        )
                                    },
                                    onDismissRequest = { dismissCallback() },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            openAuthenticationDialog.value = false
                                            connectionsClient.acceptConnection(
                                                connectionInformation.value.endpointId,
                                                payloadCallback
                                            )
                                        }) {
                                            Text(text = "Accept")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { dismissCallback() }) {
                                            Text(text = "Decline")
                                        }
                                    })
                            }
                        }

                        ConnectionStateConstants.AUTHENTICATION_OK -> {
                            Text(text = "Authentication successful!")
                        }

                        ConnectionStateConstants.AUTHENTICATION_DENIED -> {
                            Text(text = "Authentication failed!")
                        }

                        ConnectionStateConstants.CONNECTION_ESTABLISHED -> {
                            Text(text = "Connection established!")
                            mainActivityViewModel.updateConnectedToTV(connectedToTV = true)
                        }

                        ConnectionStateConstants.CONNECTION_DENIED -> {
                            Text(text = "Connection denied!")
                            mainActivityViewModel.updateConnectedToTV(connectedToTV = false)
                        }

                        ConnectionStateConstants.ERROR -> {
                            Text(text = "Some error occurred. It may help to kill the app and retry.")
                            mainActivityViewModel.updateConnectedToTV(connectedToTV = false)
                        }

                        else -> {
                            Text(text = "We're confused right now...")
                        }
                    }
                }
            }, bottomBar = {
                BottomAppBar(actions = {}, floatingActionButton = {
                    ExtendedFloatingActionButton(text = { Text(text = "Cancel") },
                        icon = {
                            Icon(
                                Icons.Default.Clear, "Cancel"
                            )
                        },
                        onClick = {
                            connectionsClient.stopDiscovery()
                            connectionsClient.stopAllEndpoints()
                            mainActivityViewModel.resetDiscoveredDevices()
                            pendingEndpoints.clear()
                            establishedEndpoints.clear()
                            mainActivityViewModel.updateConnectionUIState(ConnectionStateConstants.IDLE)
                            destinationsNavigator.navigateUp()
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    )
                })
            })
        }
    }
}
