package com.exner.tools.activitytimercompanion.ui.destinations

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exner.tools.activitytimercompanion.R
import com.exner.tools.activitytimercompanion.network.Permissions
import com.exner.tools.activitytimercompanion.network.TimerEndpoint
import com.exner.tools.activitytimercompanion.ui.BodyText
import com.exner.tools.activitytimercompanion.ui.ConnectionViewModel
import com.exner.tools.activitytimercompanion.ui.DefaultSpacer
import com.exner.tools.activitytimercompanion.ui.EndpointConnectionInformation
import com.exner.tools.activitytimercompanion.ui.ProcessState
import com.exner.tools.activitytimercompanion.ui.ProcessStateConstants
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.EditorFrontDoorDestination
import com.ramcosta.composedestinations.generated.destinations.WelcomeDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalPermissionsApi::class)
@Destination<RootGraph>
@Composable
fun Connection(
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val permissions = Permissions(context = context)

    val permissionsNeeded =
        rememberMultiplePermissionsState(
            permissions = permissions.getAllNecessaryPermissionsAsListOfStrings(),
            onPermissionsResult = { results ->
                results.forEach { result ->
                    Log.d("C PERMISSIONS", "${result.key} : ${result.value}")
                }
            }
        )

    val processState by connectionViewModel.processStateFlow.collectAsState()

    val connectionsClient = Nearby.getConnectionsClient(context)
    connectionViewModel.provideConnectionsClient(connectionsClient = connectionsClient)

    val discoveredEndpoints: List<TimerEndpoint> by connectionViewModel.endpointsFound.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
            Log.d("TEDC", "On Endpoint Found... $endpointId / ${endpointInfo.endpointName}")
            val endpoint = TimerEndpoint(endpointId, endpointInfo.endpointName)
            connectionViewModel.addDiscoveredEndpointToList(endpoint)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("TEDC", "On Endpoint Lost... $endpointId")
            connectionViewModel.removeDiscoveredEndpointFromList(endpointId)
        }
    }
    connectionViewModel.provideEndpointDiscoveryCallback(endpointDiscoveryCallback)

    val connectionInfo = connectionViewModel.connectionInfo.collectAsStateWithLifecycle()

    // some sanity checking for state
    if (processState.currentState == ProcessStateConstants.AWAITING_PERMISSIONS && permissionsNeeded.allPermissionsGranted) {
        connectionViewModel.triggerTransitionToNewState(ProcessStateConstants.PERMISSIONS_GRANTED)
    } else if (processState.currentState == ProcessStateConstants.AWAITING_PERMISSIONS) {
        Log.d(
            "STND",
            "Missing permissions: ${permissions.getAllNecessaryPermissionsAsListOfStrings()}"
        )
    }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                ConnectionMainView(
                    processStateCurrent = processState.currentState,
                    processStateMessage = processState.message,
                    launchMultiplePermissionRequest = permissionsNeeded::launchMultiplePermissionRequest,
                    discoveredEndpoints = discoveredEndpoints,
                    navigator = navigator,
                    triggerTransitionToNewState = connectionViewModel::triggerTransitionToNewState,
                    connectionInfo = connectionInfo
                )
            }
        },
        bottomBar = {
            ConnectionBottomBar(
                navigator = navigator,
                processState = processState,
                transition = connectionViewModel::triggerTransitionToNewState
            )
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConnectionMainView(
    processStateCurrent: ProcessStateConstants,
    processStateMessage: String = "OK",
    launchMultiplePermissionRequest: () -> Unit,
    discoveredEndpoints: List<TimerEndpoint>,
    navigator: DestinationsNavigator,
    triggerTransitionToNewState: (ProcessStateConstants, String) -> Unit,
    connectionInfo: State<EndpointConnectionInformation>
) {
    val openAuthenticationDialog = remember { mutableStateOf(false) }

    //
    // Top part - status and info
    //
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f)) {
        // UI, depending on state
        when (processStateCurrent) {
            ProcessStateConstants.AWAITING_PERMISSIONS, ProcessStateConstants.PERMISSIONS_DENIED -> {
                Text(text = "If you would like to send processes to your TV running Activity Timer, this app needs permission for Bluetooth, WiFi, and the discovery of nearby devices, which may also need location permissions.")
                DefaultSpacer()
                Button(
                    onClick = {
                        launchMultiplePermissionRequest()
                    }
                ) {
                    Text(text = "Request permissions")
                }
            }

            ProcessStateConstants.PERMISSIONS_GRANTED -> {
                Text(text = "All permissions OK.")
            }

            ProcessStateConstants.STARTING_DISCOVERY -> {
                Text(text = "Looking for a TV now...")
                DefaultSpacer()
                Text(text = "Make sure the Activity Timer app is running on your TV!")
            }

            ProcessStateConstants.DISCOVERY_STARTED -> {
                Text(text = "Looking for a TV running Activity Timer... once found, tap to connect.")
            }

            ProcessStateConstants.PARTNER_CHOSEN -> {
                Text(text = "Connecting to partner $processStateMessage...")
            }

            ProcessStateConstants.CONNECTING -> {
                Text(text = "Connecting to partner $processStateMessage...")
            }

            ProcessStateConstants.AUTHENTICATION_REQUESTED -> {
                openAuthenticationDialog.value = true
                val dismissCallback = {
                    openAuthenticationDialog.value = false
                    triggerTransitionToNewState(
                        ProcessStateConstants.AUTHENTICATION_DENIED,
                        "Denied"
                    )
                }
                if (openAuthenticationDialog.value) {
                    AlertDialog(
                        title = { Text(text = "Accept connection to " + connectionInfo.value.endpointName) },
                        text = { Text(text = "Confirm the code matches on both devices: " + connectionInfo.value.authenticationDigits) },
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
                                triggerTransitionToNewState(
                                    ProcessStateConstants.AUTHENTICATION_OK,
                                    "Accepted"
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

            ProcessStateConstants.AUTHENTICATION_OK -> {}
            ProcessStateConstants.AUTHENTICATION_DENIED -> {}

            ProcessStateConstants.CONNECTION_ESTABLISHED -> {
                // TODO
                navigator.navigate(EditorFrontDoorDestination())
            }

            ProcessStateConstants.CONNECTION_DENIED -> {}

            ProcessStateConstants.DATA_RECEIVED -> {
                Text(text = "Data received.")
            }
//                    ProcessStateConstants.SENDING -> {}

            ProcessStateConstants.DONE -> {
                Text(text = "All done.")
            }

            ProcessStateConstants.CANCELLED -> {
                Text(text = "Cancelled.")
            }

            ProcessStateConstants.ERROR -> {
                Text(text = "Some error occurred. It may help to move away from this screen and try it all again.")
                DefaultSpacer()
                Text(text = processStateMessage)
            }
        }
    }
    //
    // middle part - found partner(s)
    //
    DefaultSpacer()
    LazyColumn( modifier = Modifier.fillMaxWidth().fillMaxHeight(0.3f)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_hourglass_empty_24),
                    contentDescription = null
                )
                DefaultSpacer()
                Text(text = "No TV found so far...")
            }
        }
        items(
            items = discoveredEndpoints,
            key = { it.endpointId }) { endpoint ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_tv_24),
                    contentDescription = null
                )
                DefaultSpacer()
                Box(modifier = Modifier
                    .padding(PaddingValues(8.dp))
                    .clickable {
                        triggerTransitionToNewState(
                            ProcessStateConstants.PARTNER_CHOSEN,
                            endpoint.endpointId
                        )
                    }) {
                    Text(text = "Activity Timer for TV ID: ${endpoint.endpointId}")
                }
            }
        }
    }
    //
    // bottom part - BottomBar
    //

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
//                ProcessStateConstants.AUTHENTICATION_OK -> {
//                    ExtendedFloatingActionButton(
//                        text = { Text(text = "Send") },
//                        icon = {
//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = "Send Processes"
//                            )
//                        },
//                        onClick = {
//                            transition(ProcessStateConstants.SENDING, "Sending")
//                        },
//                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
//                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
//                    )
//                }
                ProcessStateConstants.DATA_RECEIVED -> {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Manage") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Manage"
                            )
                        },
                        onClick = {
                            navigator.navigate(EditorFrontDoorDestination)
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
                            navigator.popBackStack(WelcomeDestination, inclusive = false)
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