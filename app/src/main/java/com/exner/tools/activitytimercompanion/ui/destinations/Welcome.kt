package com.exner.tools.activitytimercompanion.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.exner.tools.activitytimercompanion.ui.BodyText
import com.exner.tools.activitytimercompanion.ui.HeaderText
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ConnectionDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>(start = true)
@Composable
fun Welcome(
    navigator: DestinationsNavigator
) {

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxWidth()
            ) {
                HeaderText(text = "Connect")
                BodyText(text = "Connect to Activity Timer for TV")
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(text = "Connect") },
                        icon = {
                            Icon(Icons.Default.Call, "Connect to Activity Timer on TV")
                        },
                        onClick = {
                            navigator.navigate(
                                ConnectionDestination()
                            )
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    )
                }
            )
        }
            )
}