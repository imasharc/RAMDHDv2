package com.sharc.ramdhd.data.repository

import com.sharc.ramdhd.data.dao.RoutineDao
import com.sharc.ramdhd.data.model.Routine
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.model.Step
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class RoutineRepository(private val routineDao: RoutineDao) {
    val allRoutines: Flow<List<RoutineWithSteps>> = routineDao.getAllRoutinesWithSteps()

    fun getRoutineWithSteps(routineId: Int): Flow<RoutineWithSteps?> {
        return routineDao.getRoutineWithSteps(routineId)
    }

    suspend fun insert(routine: Routine, steps: List<Step>) {
        routineDao.insertRoutineWithSteps(routine, steps)
    }

    suspend fun update(routine: Routine) {
        routineDao.updateRoutine(routine)
    }

    suspend fun updateStepCheckedState(stepId: Int, isChecked: Boolean) {
        routineDao.updateStepCheckedState(stepId, isChecked)
    }

    suspend fun areAllStepsChecked(routineId: Int): Boolean {
        val checkedCount = routineDao.getCheckedStepsCount(routineId)
        val totalCount = routineDao.getTotalStepsCount(routineId)
        return checkedCount == totalCount && totalCount > 0
    }

    suspend fun resetRoutineSteps(routineId: Int) {
        routineDao.resetRoutineSteps(routineId)
    }

    suspend fun markRoutineAsCompleted(routineId: Int) {
        routineDao.markRoutineAsCompleted(routineId)
    }

    suspend fun markRoutineAsNotCompleted(routineId: Int) {
        routineDao.markRoutineAsNotCompleted(routineId)
    }

    suspend fun resetRoutine(routineId: Int) {
        // Reset all steps to unchecked
        routineDao.resetRoutineSteps(routineId)
        // Mark routine as not completed
        routineDao.markRoutineAsNotCompleted(routineId)
    }

    suspend fun delete(routines: List<Routine>) {
        routineDao.deleteRoutines(routines)
    }
}