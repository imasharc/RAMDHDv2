package com.sharc.ramdhd.ui.dashboard.graphTasks.editSingle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.graphTask.GraphTask
import com.sharc.ramdhd.data.model.graphTask.GraphStep
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import com.sharc.ramdhd.data.repository.graphTask.GraphTaskRepository
import java.time.LocalDateTime

class EditGraphTaskViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "EditGraphTaskViewModel"
        private const val MIN_STEPS = 3
    }

    private val _steps = MutableLiveData<MutableList<String>>()
    private val repository: GraphTaskRepository
    private var taskId: Int = -1
    private var currentSteps = MutableList(MIN_STEPS) { "" }
    private var gratificationStepIndices = mutableSetOf<Int>()  // Changed to Set to store multiple indices

    init {
        Log.d(TAG, "Initializing EditGraphTaskViewModel")
        val database = AppDatabase.getDatabase(application)
        repository = GraphTaskRepository(database.graphTaskDao())
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
        gratificationStepIndices.clear()  // Reset gratification steps
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

    fun updateStepGratification(index: Int, isGratification: Boolean) {
        if (isGratification) {
            gratificationStepIndices.add(index)
        } else {
            gratificationStepIndices.remove(index)
        }
        Log.d(TAG, "Gratification steps updated: $gratificationStepIndices")
    }

    fun isStepGratification(index: Int): Boolean {
        return gratificationStepIndices.contains(index)
    }

    suspend fun saveGraphTask(title: String, description: String): GraphTaskWithSteps {
        Log.d(TAG, "Saving graph task: $title")

        try {
            // Find the last index with non-empty text
            var lastWrittenIndex = currentSteps.size - 1
            while (lastWrittenIndex >= 0 && currentSteps[lastWrittenIndex].isEmpty()) {
                lastWrittenIndex--
            }

            // Keep all steps up to the last written one
            val stepsToSave = currentSteps.take(lastWrittenIndex + 1).mapIndexed { index, stepText ->
                GraphStep(
                    taskId = taskId,
                    orderNumber = index,
                    description = stepText,
                    isFinishing = index == lastWrittenIndex,  // Last step is finishing step
                    isGratification = gratificationStepIndices.contains(index)  // Check if this step is marked for gratification
                )
            }

            // Validate minimum steps requirement
            if (stepsToSave.size < MIN_STEPS) {
                throw IllegalStateException("At least $MIN_STEPS steps are required")
            }

            Log.d(TAG, "Steps to save: ${stepsToSave.joinToString {
                "${it.description} (Finishing: ${it.isFinishing}, Gratification: ${it.isGratification})"
            }}")

            return if (taskId != -1) {
                val updatedTask = GraphTask(
                    id = taskId,
                    title = title,
                    description = description,
                    timestamp = LocalDateTime.now(),
                    isSelected = false
                )
                repository.updateGraphTaskWithSteps(updatedTask, stepsToSave)
                Log.d(TAG, "Updated existing task with ID: $taskId")
                GraphTaskWithSteps(updatedTask, stepsToSave)
            } else {
                val newTask = GraphTask(
                    title = title,
                    description = description,
                    timestamp = LocalDateTime.now(),
                    isSelected = false
                )
                repository.insert(newTask, stepsToSave)
                Log.d(TAG, "Created new task")
                GraphTaskWithSteps(newTask, stepsToSave)
            }.also { result ->
                Log.d(TAG, """Successfully saved graph task:
                    Title: ${result.task.title}
                    Description: ${result.task.description}
                    Steps: ${result.steps.joinToString {
                    "${it.description} (Finishing: ${it.isFinishing}, Gratification: ${it.isGratification})"
                }}""".trimIndent())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving graph task: ${e.message}", e)
            throw e
        }
    }

    fun setTaskId(id: Int) {
        this.taskId = id
    }
}