package com.exner.tools.activitytimercompanion.ui.destinations

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.exner.tools.activitytimercompanion.network.Permissions
import com.exner.tools.activitytimercompanion.network.TimerEndpoint
import com.exner.tools.activitytimercompanion.ui.ConnectionViewModel
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exner.tools.activitytimercompanion.ui.BodyText
import com.exner.tools.activitytimercompanion.ui.DefaultSpacer
import com.exner.tools.activitytimercompanion.ui.EndpointConnectionInformation
import com.exner.tools.activitytimercompanion.ui.MainActivityViewModel
import com.exner.tools.activitytimercompanion.ui.ProcessState
import com.exner.tools.activitytimercompanion.ui.ProcessStateConstants
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class)
@Destination<RootGraph>
@Composable
fun Connection(
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val permissions = Permissions(context = context)

    val permissionsNeeded =
        rememberMultiplePermissionsState(
            permissions = permissions.getAllNecessaryPermissionsAsListOfStrings()
        )

    val processState by connectionViewModel.processStateFlow.collectAsState()

    val connectionsClient = Nearby.getConnectionsClient(context)
    connectionViewModel.provideConnectionsClient(connectionsClient = connectionsClient)

    val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
            Log.d("TEDC", "On Endpoint Found... $endpointId / ${endpointInfo.endpointName}")

            // An endpoint was found. We request a connection to it.
            val endpoint = TimerEndpoint(endpointId, endpointInfo.endpointName)

            connectionsClient.requestConnection(
                endpoint.userName,
                endpoint.endpointId,
                connectionViewModel.timerLifecycleCallback
            )
                .addOnSuccessListener { _: Void? ->
                    Log.d("TEDC", "Connection request succeeded!")
                    mainActivityViewModel.updateConnectedToTV(connectedToTV = true)
                }
                .addOnFailureListener { e: Exception? ->
                    if (e != null) {
                        Log.d("TEDC", "Connection failed: ${e.message}")
                    }
                    mainActivityViewModel.updateConnectedToTV(connectedToTV = false)
                }

        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("TEDC", "On Endpoint Lost... $endpointId")
            mainActivityViewModel.updateConnectedToTV(connectedToTV = false)
        }
    }
    connectionViewModel.provideEndpointDiscoveryCallback(endpointDiscoveryCallback)

    val discoveredEndpoints: List<TimerEndpoint> by connectionViewModel.endpointsFound.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    val openAuthenticationDialog = remember { mutableStateOf(false) }
    val connectionInfo by connectionViewModel.connectionInfo.collectAsState()

    // some sanity checking for state
    if (processState.currentState == ProcessStateConstants.AWAITING_PERMISSIONS && permissionsNeeded.allPermissionsGranted) {
        connectionViewModel.transitionToNewState(ProcessStateConstants.PERMISSIONS_GRANTED)
    } else if (processState.currentState == ProcessStateConstants.AWAITING_PERMISSIONS) {
        Log.d("STND", "Missing permissions: ${permissions.getAllNecessaryPermissionsAsListOfStrings()}")
    }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                // UI, depending on state
                when (processState.currentState) {
                    ProcessStateConstants.AWAITING_PERMISSIONS -> {
                        ProcessStateAwaitingPermissionsScreen(permissionsNeeded)
                    }

                    ProcessStateConstants.PERMISSIONS_GRANTED -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "All permissions OK.")
                        }
                    }

                    ProcessStateConstants.PERMISSIONS_DENIED -> {
                        ProcessStateAwaitingPermissionsScreen(permissionsNeeded)
                    }

                    ProcessStateConstants.STARTING_DISCOVERY -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "Looking for a TV now...")
                            DefaultSpacer()
                            Text(text = "Make sure the Activity Timer app is running on your TV!")
                        }
                    }

                    ProcessStateConstants.DISCOVERY_STARTED -> {
                        ProcessStateDiscoveryStartedScreen(discoveredEndpoints) { endpointId ->
                            connectionViewModel.transitionToNewState(
                                ProcessStateConstants.PARTNER_CHOSEN,
                                endpointId
                            )
                        }
                    }

                    ProcessStateConstants.PARTNER_CHOSEN -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "Connecting to partner ${processState.message}...")
                        }
                    }

                    ProcessStateConstants.CONNECTING -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "Connecting to partner ${processState.message}...")
                        }
                    }

                    ProcessStateConstants.AUTHENTICATION_REQUESTED -> {
                        openAuthenticationDialog.value = true
                        ProcessStateAuthenticationRequestedScreen(
                            openAuthenticationDialog = openAuthenticationDialog.value,
                            info = connectionInfo,
                            confirmCallback = {
                                openAuthenticationDialog.value = false
                                connectionViewModel.transitionToNewState(
                                    ProcessStateConstants.AUTHENTICATION_OK,
                                    "Accepted"
                                )
                            },
                            dismissCallback = {
                                openAuthenticationDialog.value = false
                                connectionViewModel.transitionToNewState(
                                    ProcessStateConstants.AUTHENTICATION_DENIED,
                                    "Denied"
                                )
                            }
                        )
                    }

                    ProcessStateConstants.AUTHENTICATION_OK -> {}
                    ProcessStateConstants.AUTHENTICATION_DENIED -> {}

                    ProcessStateConstants.CONNECTION_ESTABLISHED -> {
                        // TODO
                        mainActivityViewModel.updateConnectedToTV(connectedToTV = true)
                    }

                    ProcessStateConstants.CONNECTION_DENIED -> {}
                    ProcessStateConstants.SENDING -> {}

                    ProcessStateConstants.DONE -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "All done.")
                        }
                    }

                    ProcessStateConstants.CANCELLED -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "Cancelled.")
                        }
                        mainActivityViewModel.updateConnectedToTV(connectedToTV = false)
                    }

                    ProcessStateConstants.ERROR -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "Some error occurred. It may help to move away from this screen and try it all again.")
                            DefaultSpacer()
                            Text(text = processState.message)
                        }
                        mainActivityViewModel.updateConnectedToTV(connectedToTV = false)
                    }

                }
            }
        },
        bottomBar = {
            ConnectionBottomBar(
                navigator = navigator,
                processState = processState,
                transition = connectionViewModel::transitionToNewState
            )
        }
    )
}

@Composable
fun ProcessStateAuthenticationRequestedScreen(
    openAuthenticationDialog: Boolean,
    info: EndpointConnectionInformation,
    confirmCallback: () -> Unit,
    dismissCallback: () -> Unit
) {
    if (openAuthenticationDialog) {
        AlertDialog(
            title = { Text(text = "Accept connection to " + info.endpointName) },
            text = { Text(text = "Confirm the code matches on both devices: " + info.authenticationDigits) },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "Alert") },
            onDismissRequest = { dismissCallback() },
            confirmButton = { TextButton(onClick = { confirmCallback() }) {
                Text(text = "Accept")
            } },
            dismissButton = { TextButton(onClick = { dismissCallback() }) {
                Text(text = "Decline")
            } }
        )
    }
}

@Composable
fun ProcessConnectionEstablished(
    onItemClick : (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(PaddingValues(8.dp))
            .fillMaxSize()
    ) {
        item {
            Text(text = "Connected to ")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ProcessStateAwaitingPermissionsScreen(permissionsNeeded: MultiplePermissionsState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "If you would like to send processes to your TV running Activity Timer, this app needs permission for Bluetooth, WiFi, and the discovery of nearby devices, which may also need location permissions.")
        DefaultSpacer()
        Button(
            onClick = {
                permissionsNeeded.launchMultiplePermissionRequest()
            }
        ) {
            Text(text = "Request permissions")
        }
    }
}

@Composable
private fun ProcessStateDiscoveryStartedScreen(
    discoveredEndpoints: List<TimerEndpoint>,
    onItemClick : (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(PaddingValues(8.dp))
            .fillMaxSize()
    ) {
        item {
            Text(text = "Looking for a TV running Activity Timer... once found, tap to connect.")
        }
        items(items = discoveredEndpoints, key = {it.endpointId}) { endpoint ->
            Box(modifier = Modifier
                .padding(PaddingValues(8.dp))
                .clickable {
                    onItemClick(endpoint.endpointId)
                }) {
                BodyText(text = "Activity Timer for TV ID: ${endpoint.endpointId}")
            }
        }
    }
}

@Composable
fun ConnectionBottomBar(
    navigator: DestinationsNavigator,
    processState: ProcessState,
    transition: (ProcessStateConstants, String) -> Unit
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = {
                transition(ProcessStateConstants.CANCELLED, "Cancelled")
            }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Cancel"
                )
            }
        },
        floatingActionButton = {
            when (processState.currentState) {
                ProcessStateConstants.AWAITING_PERMISSIONS -> {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Request Permissions") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Request permissions"
                            )
                        },
                        onClick = {
                            // how do we do this?
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    )
                }
                ProcessStateConstants.PERMISSIONS_GRANTED -> {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Discover Devices") },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                                contentDescription = "Discover Devices"
                            )
                        },
                        onClick = {
                            transition(ProcessStateConstants.DISCOVERY_STARTED, "Discovering")
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    )
                }
                ProcessStateConstants.AUTHENTICATION_OK -> {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Send") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Send Processes"
                            )
                        },
                        onClick = {
                            transition(ProcessStateConstants.SENDING, "Sending")
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    )
                }
                ProcessStateConstants.CONNECTION_ESTABLISHED -> {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Done") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done"
                            )
                        },
                        onClick = {
                            transition(ProcessStateConstants.DONE, "Done")
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    )
                }
                ProcessStateConstants.DONE, ProcessStateConstants.CANCELLED -> {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Go back") },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        },
                        onClick = {
                            navigator.navigateUp()
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    )
                }
                else -> {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Cancel") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Cancel"
                            )
                        },
                        onClick = {
                            transition(ProcessStateConstants.CANCELLED, "Cancel")
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    )
                }
            }
        }
    )
}