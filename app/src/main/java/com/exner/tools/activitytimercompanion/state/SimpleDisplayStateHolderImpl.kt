package com.exner.tools.activitytimercompanion.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class SimpleDisplayStateHolderImpl @Inject constructor() : SimpleDisplayStateHolder {
    private val _simpleDisplayState = MutableStateFlow(SimpleDisplayState())
    override val simpleDisplayState: StateFlow<SimpleDisplayState> = _simpleDisplayState

    override fun updateSimpleDisplayState(isScreenSimple: Boolean) {
        _simpleDisplayState.update { current ->
            current.copy(isScreenSimple = isScreenSimple)
        }
    }
}