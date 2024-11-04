package com.sharc.ramdhd.data.dao

import androidx.room.*
import com.sharc.ramdhd.data.model.Routine
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.model.Step
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Transaction
    @Query("SELECT * FROM routines")
    fun getAllRoutinesWithSteps(): Flow<List<RoutineWithSteps>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<Step>)

    @Transaction
    suspend fun insertRoutineWithSteps(routine: Routine, steps: List<Step>) {
        val routineId = insertRoutine(routine)
        val stepEntities = steps.map { it.copy(routineId = routineId.toInt()) }
        insertSteps(stepEntities)
    }

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutines(routines: List<Routine>)
}