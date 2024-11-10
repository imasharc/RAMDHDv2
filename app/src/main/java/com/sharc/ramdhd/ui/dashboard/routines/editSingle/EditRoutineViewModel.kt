package com.sharc.ramdhd.ui.dashboard.routines.editSingle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.Routine
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.model.Step
import com.sharc.ramdhd.data.repository.RoutineRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EditRoutineViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "EditRoutineViewModel"
        private const val MIN_STEPS = 3
    }

    private val _steps = MutableLiveData<MutableList<String>>()
    private val repository: RoutineRepository
    private var routineId: Int = -1
    private var existingRoutine: Routine? = null
    private var currentSteps = MutableList(MIN_STEPS) { "" }

    init {
        Log.d(TAG, "Initializing EditRoutineViewModel")
        val database = AppDatabase.getDatabase(getApplication<Application>())
        repository = RoutineRepository(database.routineDao())
        // Don't set initial empty steps here
    }

    fun getSteps(): LiveData<MutableList<String>> = _steps

    fun loadRoutine(routineId: Int) {
        this.routineId = routineId
        Log.d(TAG, "Loading routine with ID: $routineId")

        viewModelScope.launch {
            try {
                repository.getRoutineWithStepsOnce(routineId)?.let { routineWithSteps ->
                    Log.d(TAG, """
                        Loaded routine data:
                        Title: ${routineWithSteps.routine.title}
                        Description: ${routineWithSteps.routine.description}
                        Steps count: ${routineWithSteps.steps.size}
                    """.trimIndent())

                    // Convert steps to list of strings and sort by order
                    currentSteps = routineWithSteps.steps
                        .sortedBy { it.orderNumber }
                        .map { it.description }
                        .toMutableList()

                    Log.d(TAG, "Sorted steps: ${
                        currentSteps.mapIndexed { index, text ->
                            "\n  $index: $text"
                        }
                    }")

                    // Ensure minimum number of steps
                    while (currentSteps.size < MIN_STEPS) {
                        currentSteps.add("")
                    }

                    // Update LiveData with the loaded steps
                    _steps.postValue(currentSteps)
                    Log.d(TAG, "Posted steps to LiveData: ${currentSteps.joinToString(", ")}")
                } ?: run {
                    Log.w(TAG, "No routine found with ID: $routineId")
                    // Initialize with empty steps if no routine found
                    _steps.postValue(MutableList(MIN_STEPS) { "" })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading routine: ${e.message}", e)
                // Initialize with empty steps on error
                _steps.postValue(MutableList(MIN_STEPS) { "" })
            }
        }
    }

    fun updateStep(index: Int, text: String) {
        try {
            Log.d(TAG, "Updating step $index with text: $text")

            if (index >= currentSteps.size) {
                currentSteps.add(text)
                Log.d(TAG, "Added new step at index $index")
            } else {
                currentSteps[index] = text
                Log.d(TAG, "Updated existing step at index $index")
            }

            _steps.value = currentSteps.toMutableList()
            Log.d(TAG, "Current steps after update: ${
                currentSteps.mapIndexed { i, t ->
                    "\n  $i: $t"
                }
            }")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating step: ${e.message}", e)
        }
    }

    suspend fun saveRoutine(title: String, description: String): RoutineWithSteps {
        Log.d(TAG, """
            Saving routine:
            ID: $routineId
            Title: $title
            Description: $description
        """.trimIndent())

        try {
            // Find the last index with non-empty text
            var lastWrittenIndex = currentSteps.size - 1
            while (lastWrittenIndex >= 0 && currentSteps[lastWrittenIndex].isEmpty()) {
                lastWrittenIndex--
            }

            // Keep all steps up to the last written one (including empty ones)
            val stepsToSave = currentSteps.take(lastWrittenIndex + 1).mapIndexed { index, stepText ->
                Step(
                    routineId = routineId,
                    orderNumber = index,
                    description = stepText
                )
            }

            Log.d(TAG, "Steps to save (including empty ones): ${
                stepsToSave.mapIndexed { index, step ->
                    "\n  $index: ${step.description}"
                }
            }")

            return if (routineId != -1) {
                // Update existing routine
                Log.d(TAG, "Updating existing routine with ID: $routineId")
                val updatedRoutine = Routine(
                    id = routineId,
                    title = title,
                    description = description,
                    timestamp = LocalDateTime.now(),
                    isSelected = false
                )
                repository.updateRoutineWithSteps(updatedRoutine, stepsToSave)
                RoutineWithSteps(updatedRoutine, stepsToSave)
            } else {
                // Create new routine
                Log.d(TAG, "Creating new routine")
                val newRoutine = Routine(
                    title = title,
                    description = description,
                    timestamp = LocalDateTime.now(),
                    isSelected = false
                )
                repository.insert(newRoutine, stepsToSave)
                RoutineWithSteps(newRoutine, stepsToSave)
            }.also { result ->
                Log.d(TAG, "Successfully saved routine with ${result.steps.size} steps")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving routine: ${e.message}", e)
            throw e
        }
    }
}