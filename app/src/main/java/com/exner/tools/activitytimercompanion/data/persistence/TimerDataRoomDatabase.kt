package com.exner.tools.activitytimercompanion.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TimerProcess::class,TimerProcessCategory::class],
    views = [TimerCategoryIdNameCount::class],
    version = 4,
    exportSchema = false
)
abstract class TimerDataRoomDatabase : RoomDatabase() {
    abstract fun processDAO(): TimerDataDAO
}