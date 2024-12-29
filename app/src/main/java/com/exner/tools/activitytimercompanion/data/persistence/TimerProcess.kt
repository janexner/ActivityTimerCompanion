package com.exner.tools.activitytimercompanion.data.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class TimerProcess(
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "info") val info : String,
    @ColumnInfo(name = "uuid") val uuid : String,

    @Json(name = "process_time")
    @ColumnInfo(name = "process_time") val processTime : Int = 30,
    @Json(name = "interval_time")
    @ColumnInfo(name = "interval_time") val intervalTime : Int = 5,

    @Json(name = "has_auto_chain")
    @ColumnInfo(name = "has_auto_chain") val hasAutoChain : Boolean = false,
    @Json(name = "goto_uuid")
    @ColumnInfo(name = "goto_uuid") val gotoUuid : String?,
    @Json(name = "goto_name")
    @ColumnInfo(name = "goto_name") val gotoName : String?,

    @Json(name = "category_id")
    @ColumnInfo(name = "category_id") val categoryId : Long = -1L,

    @Json(name = "background_uri")
    @ColumnInfo(name = "background_uri") val backgroundUri : String?,

    @PrimaryKey(autoGenerate = true) val uid : Long = 0
)