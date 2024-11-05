package com.sharc.ramdhd.ui.dashboard.routines

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.Routine
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RoutineMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository
    val routines: Flow<List<RoutineWithSteps>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RoutineRepository(database.routineDao())
        routines = repository.allRoutines
    }

    fun deleteRoutines(routines: List<Routine>) {
        viewModelScope.launch {
            repository.delete(routines)
        }
    }
}