package com.exner.tools.activitytimercompanion.ui.destinations

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exner.tools.activitytimercompanion.ui.MainActivityViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AboutDestination
import com.ramcosta.composedestinations.generated.destinations.CategoryListDestination
import com.ramcosta.composedestinations.generated.destinations.ProcessListDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsDestination
import com.ramcosta.composedestinations.generated.destinations.WelcomeDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator

@Composable
fun ActivityTimerCompanionGlobalScaffold(
    mainActivityViewModel: MainActivityViewModel = hiltViewModel()
) {
    val engine = rememberNavHostEngine()
    val navController = engine.rememberNavController()
    val destinationsNavigator = navController.rememberDestinationsNavigator()
    val destination = navController.currentDestinationAsState().value

    val connectedToTV = mainActivityViewModel.tvConnectionStateHolder.tvConnectionState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ActivityTimerCompanionTopBar(destination, destinationsNavigator, connectedToTV.value.isConnectedToTV)
        },
        content = { innerPadding ->
            val newPadding = PaddingValues.Absolute(
                innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                innerPadding.calculateTopPadding(),
                innerPadding.calculateRightPadding(LayoutDirection.Ltr),
                0.dp
            )
            DestinationsNavHost(
                navController = navController,
                navGraph = NavGraphs.root,
                modifier = Modifier.padding(newPadding)
            ) {
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityTimerCompanionTopBar(
    destination: DestinationSpec?,
    destinationsNavigator: DestinationsNavigator,
    manageMenusVisible: Boolean = false
) {
    var displayMainMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = "Activity Timer Companion") },
        navigationIcon = {
            when (destination) {
                WelcomeDestination -> {
                    // no back button here
                }

                else -> {
                    IconButton(onClick = { destinationsNavigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = {
                    displayMainMenu = !displayMainMenu
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
                expanded = displayMainMenu,
                onDismissRequest = { displayMainMenu = false }
            ) {
                if (manageMenusVisible) {
                    DropdownMenuItem(
                        enabled = destination != ProcessListDestination,
                        text = {
                            Text(
                                text = "Manage processes",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            displayMainMenu = false
                            destinationsNavigator.navigate(ProcessListDestination())
                        }
                    )
                    DropdownMenuItem(
                        enabled = destination != CategoryListDestination,
                        text = {
                            Text(
                                text = "Manage categories",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            displayMainMenu = false
                            destinationsNavigator.navigate(CategoryListDestination())
                        }
                    )
                }
                DropdownMenuItem(
                    enabled = destination != SettingsDestination,
                    text = { Text(text = "Settings", style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        displayMainMenu = false
                        destinationsNavigator.navigate(SettingsDestination())
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    enabled = destination != AboutDestination,
                    text = {
                        Text(
                            text = "About Activity Timer Companion",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        displayMainMenu = false
                        destinationsNavigator.navigate(AboutDestination())
                    }
                )
            }
        }
    )
}
