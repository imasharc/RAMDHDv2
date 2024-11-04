package com.sharc.ramdhd.data.repository

import com.sharc.ramdhd.data.dao.RoutineDao
import com.sharc.ramdhd.data.model.Routine
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.model.Step
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class RoutineRepository(private val routineDao: RoutineDao) {
    val allRoutines: Flow<List<RoutineWithSteps>> = routineDao.getAllRoutinesWithSteps()

    suspend fun insert(routine: Routine, steps: List<Step>) {
        routineDao.insertRoutineWithSteps(routine, steps)
    }

    suspend fun update(routine: Routine) {
        routineDao.updateRoutine(routine)
    }

    suspend fun delete(routines: List<Routine>) {
        routineDao.deleteRoutines(routines)
    }
}