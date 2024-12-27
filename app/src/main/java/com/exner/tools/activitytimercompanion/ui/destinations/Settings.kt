package com.exner.tools.activitytimercompanion.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exner.tools.activitytimercompanion.ui.SettingsViewModel
import com.exner.tools.activitytimercompanion.ui.TextAndSwitch
import com.exner.tools.activitytimercompanion.ui.TextAndTriStateToggle
import com.exner.tools.activitytimercompanion.ui.theme.Theme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import androidx.compose.runtime.getValue

@Destination<RootGraph>
@Composable
fun Settings(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {

    val userSelectedTheme by settingsViewModel.userSelectedTheme.collectAsStateWithLifecycle()
    val showSimpleDisplay by settingsViewModel.showSimpleDisplay.collectAsStateWithLifecycle()
    val chainToSameCategoryOnly by settingsViewModel.chainToSameCategoryOnly.collectAsStateWithLifecycle()

    // show vertically
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TextAndTriStateToggle(
            text = "Theme",
            currentTheme = userSelectedTheme,
            updateTheme = { it: Theme ->
                settingsViewModel.updateUserSelectedTheme(
                    it
                )
            }
        )
        TextAndSwitch(text = "Simplify Display", checked = showSimpleDisplay) {
            settingsViewModel.updateShowSimpleDisplay(it)
        }
        TextAndSwitch(
            text = "Chain to same category only",
            checked = chainToSameCategoryOnly
        ) {
            settingsViewModel.updateChainToSameCategoryOnly(it)
        }
    }

}
