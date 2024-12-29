package com.exner.tools.activitytimercompanion.ui

import androidx.lifecycle.ViewModel
import com.exner.tools.activitytimercompanion.data.persistence.TimerDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditorFrontDoorViewModel @Inject constructor(
    private val repository: TimerDataRepository
) : ViewModel() {
}