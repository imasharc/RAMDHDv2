package com.sharc.ramdhd.data.model.graphTask

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "graph_tasks")
data class GraphTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    var isSelected: Boolean = false,
    var isCompleted: Boolean = false
)