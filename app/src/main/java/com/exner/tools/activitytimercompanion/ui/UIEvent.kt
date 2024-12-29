package com.exner.tools.activitytimercompanion.ui

sealed class UIEvent {
    data class transitionState(
        val newState: ProcessStateConstants,
        val message: String = "OK"
    ): UIEvent()
}