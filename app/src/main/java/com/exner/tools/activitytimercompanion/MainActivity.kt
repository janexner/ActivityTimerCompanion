package com.exner.tools.activitytimercompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import com.exner.tools.activitytimercompanion.data.preferences.ActivityTimerCompanionUserPreferencesManager
import com.exner.tools.activitytimercompanion.ui.MainActivityViewModel
import com.exner.tools.activitytimercompanion.ui.destinations.ActivityTimerCompanionGlobalScaffold
import com.exner.tools.activitytimercompanion.ui.theme.ActivityTimerCompanionTheme
import com.exner.tools.activitytimercompanion.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesManager: ActivityTimerCompanionUserPreferencesManager

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // night mode has two possible triggers:
            // - device may be in night mode
            // - force night mode setting may be on
            val userTheme = viewModel.userSelectedTheme.collectAsState()

            ActivityTimerCompanionTheme(
                darkTheme = userTheme.value == Theme.Dark || (userTheme.value == Theme.Auto && isSystemInDarkTheme())
            ) {
                ActivityTimerCompanionGlobalScaffold()
            }
        }
    }
}

