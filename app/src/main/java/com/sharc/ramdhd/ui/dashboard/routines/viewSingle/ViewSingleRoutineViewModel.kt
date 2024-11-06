package com.sharc.ramdhd.ui.dashboard.routines.viewSingle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.repository.RoutineRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewSingleRoutineViewModel(private val repository: RoutineRepository) : ViewModel() {
    private val _routine = MutableLiveData<RoutineWithSteps>()
    val routine: LiveData<RoutineWithSteps> = _routine

    fun loadRoutine(routineId: Int) {
        viewModelScope.launch {
            Log.d("ViewModel", "Loading routine with ID: $routineId")
            repository.getRoutineWithSteps(routineId).collectLatest { routineWithSteps ->
                Log.d("ViewModel", "Received routine: ${routineWithSteps?.routine?.title}, steps: ${routineWithSteps?.steps?.size}")
                routineWithSteps?.let {
                    _routine.value = it
                }
            }
        }
    }
}