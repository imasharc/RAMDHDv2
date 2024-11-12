package com.sharc.ramdhd.data.model.graphTask

import androidx.room.Embedded
import androidx.room.Relation

data class GraphTaskWithSteps(
    @Embedded val task: GraphTask,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val steps: List<GraphStep>
)