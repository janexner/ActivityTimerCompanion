package com.exner.tools.activitytimercompanion.data.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class TimerProcessCategory(
    @ColumnInfo(name = "name") var name : String,
    @Json(name = "background_uri")
    @ColumnInfo(name = "background_uri") var backgroundUri : String?,

    @PrimaryKey(autoGenerate = true) val uid: Long = 0
)