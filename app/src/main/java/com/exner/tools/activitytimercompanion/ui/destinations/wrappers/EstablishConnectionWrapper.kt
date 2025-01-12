package com.exner.tools.activitytimercompanion.ui.destinations.wrappers

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.exner.tools.activitytimercompanion.R
import com.exner.tools.activitytimercompanion.network.EndpointConnectionInformation
import com.exner.tools.activitytimercompanion.network.TimerEndpoint
import com.exner.tools.activitytimercompanion.ui.DefaultSpacer
import com.exner.tools.activitytimercompanion.ui.IconSpacer
import com.exner.tools.activitytimercompanion.ui.ProcessStateConstants
import com.exner.tools.activitytimercompanion.ui.UIEvent
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
import kotlinx.coroutines.launch
import kotlin.text.Charsets.UTF_8

enum class ConnectionStateConstants {
    IDLE,
    STARTING_DISCOVERY,
    DISCOVERY_STARTED,
    PARTNER_CHOSEN,
    AUTHENTICATION_REQUESTED,
    AUTHENTICATION_OK,
    AUTHENTICATION_DENIED,
    CONNECTING,
    CONNECTION_ESTABLISHED,
    CONNECTION_DENIED,
    ERROR,
    DISCONNECTED
}
private const val serviceId: String = "com.exner.tools.ActivityTimer.Companion"
private const val userName: String = "Activity Timer Companion"

object EstablishConnectionWrapper : DestinationWrapper {

    @Composable
    override fun <T> DestinationScope<T>.Wrap(screenContent: @Composable () -> Unit) {

        val isAppConnectedToTV = remember { mutableStateOf(false) }
        val currentState = remember { mutableStateOf(ConnectionStateConstants.IDLE) }

        // all the UI needed during connecting
        //
        // we're going to go straight into discover w/o asking for it
        val discoveredEndpoints: MutableList<TimerEndpoint> = remember { mutableListOf() }
        val pendingEndpoints: MutableMap<String, TimerEndpoint> = remember { mutableMapOf() }
        val establishedEndpoints: MutableMap<String, TimerEndpoint> = remember { mutableMapOf() }

        val openAuthenticationDialog = remember { mutableStateOf(false) }

        // for the actual discovering
        val context = LocalContext.current
        val connectionsClient = Nearby.getConnectionsClient(context)

        var connectionInformation = remember { mutableStateOf(EndpointConnectionInformation()) }

        val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
                Log.d("TEDC", "On Endpoint Found... $endpointId / ${endpointInfo.endpointName}")
                val endpoint = TimerEndpoint(endpointId, endpointInfo.endpointName)
                discoveredEndpoints.add(endpoint)
            }

            override fun onEndpointLost(endpointId: String) {
                Log.d("TEDC", "On Endpoint Lost... $endpointId")
                discoveredEndpoints.removeIf { it.endpointId == endpointId }
            }
        }

        val timerLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(
                endpointId: String,
                connectionInfo: ConnectionInfo
            ) {
                Log.d(
                    "CVMLC",
                    "onConnectionInitiated ${connectionInfo.endpointName} / ${connectionInfo.authenticationDigits}"
                )
                // authenticate
                connectionInformation.value = EndpointConnectionInformation(
                    endpointId = endpointId,
                    endpointName = connectionInfo.endpointName,
                    authenticationDigits = connectionInfo.authenticationDigits
                )
                // now move to auth requested
                currentState.value = ConnectionStateConstants.AUTHENTICATION_REQUESTED
            }

            override fun onConnectionResult(
                endpointId: String,
                connectionResolution: ConnectionResolution
            ) {
                Log.d(
                    "CVMLC",
                    "onConnectionResult $endpointId: $connectionResolution"
                )
                if (!connectionResolution.status.isSuccess) {
                    // failed
                    var statusMessage = connectionResolution.status.statusMessage
                    if (null == statusMessage) {
                        statusMessage = "Unknown issue"
                    }
                    currentState.value = ConnectionStateConstants.AUTHENTICATION_DENIED
                } else {
                    // this worked!
                    val newEndpoint = pendingEndpoints.remove(endpointId)
                    establishedEndpoints.put(endpointId, newEndpoint!!)
                    currentState.value = ConnectionStateConstants.CONNECTION_ESTABLISHED
                }
            }

            override fun onDisconnected(endpointId: String) {
                establishedEndpoints.remove(endpointId)
                currentState.value = ConnectionStateConstants.DISCONNECTED
            }
        }

        if (currentState.value == ConnectionStateConstants.IDLE) {
            val discoveryOptions =
                DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT)
                    .build()
            connectionsClient.startDiscovery(
                serviceId,
                endpointDiscoveryCallback,
                discoveryOptions
            ).addOnSuccessListener { _ ->
                Log.d("SNDVM", "Success! Discovery started")
                currentState.value = ConnectionStateConstants.DISCOVERY_STARTED
            }.addOnFailureListener { e: Exception? ->
                val errorMessage = "Error discovering" + if (e != null) {
                    ": ${e.message}"
                } else {
                    ""
                }
                Log.d("SNDVM", errorMessage)
                currentState.value = ConnectionStateConstants.ERROR
            }
            currentState.value = ConnectionStateConstants.STARTING_DISCOVERY
        }

        val payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                Log.d("CVMPC", "Payload received ${payload.id}")
                if (payload.type == Payload.Type.BYTES) {
                    Log.d("CVMPC", "received ${payload.type}")
                } else {
                    Log.d("CVMPC", "Payload received from $endpointId but wrong type: $payload")
                }
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                Log.d("CVMPTU", "Payload Transfer Update: ${update.status}")
                when (update.status) {
                    PayloadTransferUpdate.Status.CANCELED -> {
                        Log.d("CVMPTU", "Transfer cancelled")
                    }

                    PayloadTransferUpdate.Status.FAILURE -> {
                        Log.d("CVMPTU", "Transfer failed")
                    }

                    PayloadTransferUpdate.Status.IN_PROGRESS -> {
                        Log.d("CVMPTU", "Transfer in progress")
                    }

                    PayloadTransferUpdate.Status.SUCCESS -> {
                        Log.d("CVMPTU", "Transfer successful")
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            when (currentState.value) {
                ConnectionStateConstants.IDLE -> {}
                ConnectionStateConstants.STARTING_DISCOVERY -> {
                    Text(text = "Looking for a TV now...")
                    DefaultSpacer()
                    Text(text = "Make sure the Activity Timer app is running on your TV!")
                }
                ConnectionStateConstants.DISCOVERY_STARTED -> {
                    Text(text = "Looking for a TV running Activity Timer... once found, tap to connect.")
                    DefaultSpacer()
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .align(alignment = Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    DefaultSpacer()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )  {
                        items(
                            items = discoveredEndpoints,
                            key = { it.endpointId }
                        ) { endpoint ->
                            OutlinedCard(
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                ),
                                modifier = Modifier
                                    .fillMaxSize(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.baseline_tv_24),
                                        contentDescription = null
                                    )
                                    IconSpacer()
                                    Box(modifier = Modifier
                                        .padding(PaddingValues(8.dp))
                                        .clickable {
                                            connectionsClient.stopDiscovery()
                                            connectionsClient.requestConnection(
                                                userName,
                                                endpoint.endpointId,
                                                timerLifecycleCallback
                                            )
                                            currentState.value = ConnectionStateConstants.PARTNER_CHOSEN
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
                        isAppConnectedToTV.value = false
                    }
                    if (openAuthenticationDialog.value) {
                        AlertDialog(
                            title = { Text(text = "Accept connection to " + connectionInformation.value.endpointName) },
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
                            }
                        )
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
                    isAppConnectedToTV.value = true
                }
                ConnectionStateConstants.CONNECTION_DENIED -> {
                    Text(text = "Connection denied!")
                    isAppConnectedToTV.value = false
                }
                ConnectionStateConstants.ERROR -> {
                    Text(text = "Some error occurred. It may help to kill the app and retry.")
                    isAppConnectedToTV.value = false
                }
                else -> {
                    Text(text = "We're confused right now...")
                }
            }
        }

        // if we are connected, no need for any of the above
        if (isAppConnectedToTV.value == true) {
            screenContent()
        }
    }
}
