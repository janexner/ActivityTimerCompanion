package com.exner.tools.activitytimercompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import com.exner.tools.activitytimercompanion.data.preferences.ActivityTimerCompanionUserPreferencesManager
import com.exner.tools.activitytimercompanion.state.ThemeStateHolder
import com.exner.tools.activitytimercompanion.ui.destinations.ActivityTimerCompanionGlobalScaffold
import com.exner.tools.activitytimercompanion.ui.theme.ActivityTimerCompanionTheme
import com.exner.tools.activitytimercompanion.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesManager: ActivityTimerCompanionUserPreferencesManager
    @Inject
    lateinit var themeStateHolder: ThemeStateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // night mode has two possible triggers:
            // - device may be in night mode
            // - force night mode setting may be on
            val userTheme = themeStateHolder.themeState.collectAsState()

            ActivityTimerCompanionTheme(
                darkTheme = userTheme.value.userSelectedTheme == Theme.Dark || (userTheme.value.userSelectedTheme == Theme.Auto && isSystemInDarkTheme())
            ) {
                ActivityTimerCompanionGlobalScaffold()
            }
        }
    }
}

