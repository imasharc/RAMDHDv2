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

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId")
    fun getRoutineWithSteps(routineId: Int): Flow<RoutineWithSteps?>

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

    @Query("UPDATE routines SET isCompleted = 1 WHERE id = :routineId")
    suspend fun markRoutineAsCompleted(routineId: Int)

    @Query("UPDATE steps SET isChecked = :isChecked WHERE id = :stepId")
    suspend fun updateStepCheckedState(stepId: Int, isChecked: Boolean)

    @Query("SELECT COUNT(*) FROM steps WHERE routineId = :routineId AND isChecked = 1")
    suspend fun getCheckedStepsCount(routineId: Int): Int

    @Query("SELECT COUNT(*) FROM steps WHERE routineId = :routineId")
    suspend fun getTotalStepsCount(routineId: Int): Int

    @Query("UPDATE steps SET isChecked = 0 WHERE routineId = :routineId")
    suspend fun resetRoutineSteps(routineId: Int)

    @Query("UPDATE routines SET isCompleted = 0 WHERE id = :routineId")
    suspend fun markRoutineAsNotCompleted(routineId: Int)

    @Delete
    suspend fun deleteRoutines(routines: List<Routine>)
}