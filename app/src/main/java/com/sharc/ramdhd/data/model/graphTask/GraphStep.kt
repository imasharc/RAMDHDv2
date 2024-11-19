package com.sharc.ramdhd.data.model.graphTask

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "graph_steps",
    foreignKeys = [
        ForeignKey(
            entity = GraphTask::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class GraphStep(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val orderNumber: Int,
    val description: String,
    var isGratification: Boolean = false,
    var isFinishing: Boolean = false,
    var icon: String? = null,
    var isCompleted: Boolean = false
)