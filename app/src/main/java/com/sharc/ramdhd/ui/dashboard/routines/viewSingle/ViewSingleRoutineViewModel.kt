package com.sharc.ramdhd.ui.dashboard.routines.viewSingle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.repository.RoutineRepository
import kotlinx.coroutines.launch

class ViewSingleRoutineViewModel(private val repository: RoutineRepository) : ViewModel() {

    companion object {
        private const val TAG = "ViewSingleRoutineVM"
    }

    private val _routine = MutableLiveData<RoutineWithSteps>()
    val routine: LiveData<RoutineWithSteps> = _routine

    fun loadRoutine(routineId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading routine with ID: $routineId")
                repository.getRoutineWithStepsOnce(routineId)?.let { routineWithSteps ->
                    Log.d(TAG, """
                        Loaded routine:
                        Title: ${routineWithSteps.routine.title}
                        Description: ${routineWithSteps.routine.description}
                        Steps count: ${routineWithSteps.steps.size}
                        Steps: ${routineWithSteps.steps.joinToString {
                        "\n  - ${it.orderNumber}: ${it.description}"
                    }}
                    """.trimIndent())

                    _routine.value = routineWithSteps
                } ?: Log.w(TAG, "No routine found with ID: $routineId")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading routine: ${e.message}", e)
            }
        }
    }

    fun handleStepStateChange(
        stepId: Int,
        isChecked: Boolean,
        routineId: Int,
        onCompletion: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating step $stepId checked state to: $isChecked")
                repository.updateStepCheckedState(stepId, isChecked)

                // Reload routine data after state change
                loadRoutine(routineId)

                // Check completion status
                val isCompleted = repository.areAllStepsChecked(routineId)
                Log.d(TAG, "Routine $routineId completion status: $isCompleted")
                onCompletion(isCompleted)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling step state change: ${e.message}", e)
            }
        }
    }

    fun updateStepCheckedState(stepId: Int, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating step $stepId checked state to: $isChecked")
                repository.updateStepCheckedState(stepId, isChecked)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating step checked state: ${e.message}", e)
            }
        }
    }

    suspend fun checkRoutineCompletion(routineId: Int): Boolean {
        return try {
            val isCompleted = repository.areAllStepsChecked(routineId)
            Log.d(TAG, "Checked routine $routineId completion: $isCompleted")
            isCompleted
        } catch (e: Exception) {
            Log.e(TAG, "Error checking routine completion: ${e.message}", e)
            false
        }
    }

    fun markRoutineAsCompleted(routineId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Marking routine $routineId as completed")
                repository.markRoutineAsCompleted(routineId)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking routine as completed: ${e.message}", e)
            }
        }
    }

    fun markRoutineAsNotCompleted(routineId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Marking routine $routineId as not completed")
                repository.markRoutineAsNotCompleted(routineId)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking routine as not completed: ${e.message}", e)
            }
        }
    }

    fun resetRoutine(routineId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Resetting routine $routineId")
                repository.resetRoutine(routineId)
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting routine: ${e.message}", e)
            }
        }
    }
}