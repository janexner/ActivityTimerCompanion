package com.exner.tools.activitytimercompanion.data

import com.exner.tools.activitytimercompanion.data.persistence.TimerProcess
import com.exner.tools.activitytimercompanion.data.persistence.TimerProcessCategory
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AllDataHolder(
    val processes: List<TimerProcess>,
    val categories: List<TimerProcessCategory>
)
