package com.sharc.ramdhd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "important_events")
data class ImportantEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personName: String,
    val eventTitle: String,
    val eventDate: LocalDateTime,
    val description: String
)