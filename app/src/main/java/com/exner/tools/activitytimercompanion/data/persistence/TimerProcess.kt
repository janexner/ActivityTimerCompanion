package com.exner.tools.activitytimercompanion.data.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimerProcess(
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "info") val info : String,
    @ColumnInfo(name = "uuid") val uuid : String,

    @ColumnInfo(name = "process_time") val processTime : Int = 30,
    @ColumnInfo(name = "interval_time") val intervalTime : Int = 5,

    @ColumnInfo(name = "has_auto_chain") val hasAutoChain : Boolean = false,
    @ColumnInfo(name = "goto_uuid") val gotoUuid : String?,
    @ColumnInfo(name = "goto_name") val gotoName : String?,

    @ColumnInfo(name = "category_id") val categoryId : Long = -1L,

    @ColumnInfo(name = "background_uri") val backgroundUri : String?,

    @PrimaryKey(autoGenerate = true) val uid : Long = 0
)