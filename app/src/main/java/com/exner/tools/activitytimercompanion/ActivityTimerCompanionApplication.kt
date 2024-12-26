package com.exner.tools.activitytimercompanion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.google.android.material.color.DynamicColors


@HiltAndroidApp
class ActivityTimerCompanionApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // let's try dynamic colours
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
