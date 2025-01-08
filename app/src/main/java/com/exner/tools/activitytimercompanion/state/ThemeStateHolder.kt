package com.exner.tools.activitytimercompanion.state

import com.exner.tools.activitytimercompanion.ui.theme.Theme
import kotlinx.coroutines.flow.StateFlow

interface ThemeStateHolder {
    val themeState: StateFlow<ThemeState>
    fun updateTheme(theme: Theme)
}