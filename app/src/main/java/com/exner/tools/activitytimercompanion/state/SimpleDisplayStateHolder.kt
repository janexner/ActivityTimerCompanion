package com.exner.tools.activitytimercompanion.state

import kotlinx.coroutines.flow.StateFlow

interface SimpleDisplayStateHolder {
    val simpleDisplayState: StateFlow<SimpleDisplayState>
    fun updateSimpleDisplayState(isScreenSimple: Boolean)
}