package com.sharc.ramdhd.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class RoutineWithSteps(
    @Embedded val routine: Routine,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val steps: List<Step>
)