package com.exner.tools.activitytimercompanion.data.persistence

import androidx.room.DatabaseView

@DatabaseView(
    "SELECT timerprocesscategory.uid, timerprocesscategory.name, " +
            "COUNT(timerprocess.uid) AS usageCount FROM timerprocesscategory " +
            "JOIN timerprocess ON timerprocess.category_id = timerprocesscategory.uid " +
            "GROUP BY timerprocesscategory.uid"
)
data class TimerCategoryIdNameCount(
    var uid: Long,
    var name: String?,
    var usageCount: Int
)