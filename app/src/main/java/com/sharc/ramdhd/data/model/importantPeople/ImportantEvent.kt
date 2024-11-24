package com.sharc.ramdhd.data.model.importantPeople

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "important_events")
data class ImportantEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personName: String,
    val eventType: EventType,
    val eventName: String,        // e.g., "Wedding Anniversary", "Coffee Meetup", "Weekly Call"
    val eventDate: LocalDateTime,
    val recurrenceType: RecurrenceType,
    val description: String
)