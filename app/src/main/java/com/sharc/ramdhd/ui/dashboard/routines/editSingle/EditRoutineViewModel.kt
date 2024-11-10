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
    private var currentSteps = MutableList(MIN_STEPS) { "" }

    init {
        Log.d(TAG, "Initializing EditRoutineViewModel")
        val database = AppDatabase.getDatabase(getApplication<Application>())
        repository = RoutineRepository(database.routineDao())
    }

    fun getSteps(): LiveData<MutableList<String>> = _steps

    fun initializeSteps(steps: Array<String>?) {
        Log.d(TAG, "Initializing steps: ${steps?.joinToString()}")
        currentSteps = if (steps != null) {
            steps.toMutableList().also {
                while (it.size < MIN_STEPS) {
                    it.add("")
                }
            }
        } else {
            MutableList(MIN_STEPS) { "" }
        }
        _steps.value = currentSteps
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
        } catch (e: Exception) {
            Log.e(TAG, "Error updating step: ${e.message}", e)
        }
    }

    suspend fun saveRoutine(title: String, description: String): RoutineWithSteps {
        Log.d(TAG, "Saving routine: $title")

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

            return if (routineId != -1) {
                // Update existing routine
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

    fun setRoutineId(id: Int) {
        this.routineId = id
    }
}