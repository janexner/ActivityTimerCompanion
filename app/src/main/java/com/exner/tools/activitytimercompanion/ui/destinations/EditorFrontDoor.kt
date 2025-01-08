package com.exner.tools.activitytimercompanion.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.exner.tools.activitytimercompanion.state.TVConnectionStateHolder
import com.exner.tools.activitytimercompanion.ui.DefaultSpacer
import com.exner.tools.activitytimercompanion.ui.EditorFrontDoorViewModel
import com.google.android.gms.nearby.Nearby
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.CategoryListDestination
import com.ramcosta.composedestinations.generated.destinations.ConnectionDestination
import com.ramcosta.composedestinations.generated.destinations.ProcessListDestination
import com.ramcosta.composedestinations.generated.destinations.WelcomeDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun EditorFrontDoor(
    editorFrontDoorViewModel: EditorFrontDoorViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    tvConnectionStateHolder: TVConnectionStateHolder
) {
    val context = LocalContext.current
    val connectionsClient = Nearby.getConnectionsClient(context)
    editorFrontDoorViewModel.provideConnectionsClient(connectionsClient)

    val tvConnectionState by tvConnectionStateHolder.tvConnectionState.collectAsState()
    if (tvConnectionState.isConnectedToTV == false) {
        // we should NOT be here! so let's move to the Connection screen
        navigator.navigate(ConnectionDestination)
    }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
            ) {
                Text(text = "Data is now ready. You can manage your processes and categories.")
                DefaultSpacer()
                TextButton(
                    onClick = {
                        navigator.navigate(ProcessListDestination)
                    }
                ) {
                    Text(text = "Manage Processes")
                }
                DefaultSpacer()
                TextButton(
                    onClick = {
                        navigator.navigate(CategoryListDestination)
                    }
                ) {
                    Text(text = "Manage Categories")
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Done") },
                        icon = {
                            Icon(Icons.Default.Done, "Done")
                        },
                        onClick = {
                            tvConnectionStateHolder.updateTVConnectionState(isConnectedToTV = false)
                            navigator.popBackStack(WelcomeDestination, inclusive = false)
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    )
                }
            )
        }
    )
}

