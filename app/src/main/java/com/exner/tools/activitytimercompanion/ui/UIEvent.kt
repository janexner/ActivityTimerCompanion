package com.exner.tools.activitytimercompanion.ui

sealed class UIEvent {
    data class TransitionState(
        val newState: ProcessStateConstants,
        val message: String = "OK"
    ): UIEvent()
}