package com.sharc.ramdhd.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId"])]
)
data class Step(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int,
    val orderNumber: Int,
    val description: String,
    var isChecked: Boolean = false
)