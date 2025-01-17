package com.exner.tools.activitytimercompanion.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exner.tools.activitytimercompanion.ui.ConnectionViewModel
import com.exner.tools.activitytimercompanion.ui.DefaultSpacer
import com.exner.tools.activitytimercompanion.ui.ProcessState
import com.exner.tools.activitytimercompanion.ui.ProcessStateConstants
import com.exner.tools.activitytimercompanion.ui.destinations.wrappers.AskForPermissionsWrapper
import com.google.android.gms.nearby.Nearby
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.EditorFrontDoorDestination
import com.ramcosta.composedestinations.generated.destinations.WelcomeDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>(
    wrappers = [AskForPermissionsWrapper::class]
)
@Composable
fun Connection(
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val processState by connectionViewModel.processStateFlow.collectAsState()

    val connectionsClient = Nearby.getConnectionsClient(context)
    connectionViewModel.provideConnectionsClient(connectionsClient = connectionsClient)

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

@Composable
fun ConnectionMainView(
    processStateCurrent: ProcessStateConstants,
    processStateMessage: String = "OK",
) {
    //
    // Top part - status and info
    //
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
    ) {
        // UI, depending on state
        when (processStateCurrent) {
            ProcessStateConstants.IDLE -> {
                Text(text = "All permissions OK.")
            }

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
    DefaultSpacer()
    //
    // bottom part - BottomBar
    //

}

@Composable
fun ConnectionBottomBar(
    navigator: DestinationsNavigator,
    processState: ProcessState,
    transition: (ProcessStateConstants) -> Unit
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = {
                transition(ProcessStateConstants.CANCELLED)
            }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Cancel"
                )
            }
        },
        floatingActionButton = {
            when (processState.currentState) {
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
                            transition(ProcessStateConstants.CANCELLED)
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    )
                }
            }
        }
    )
}