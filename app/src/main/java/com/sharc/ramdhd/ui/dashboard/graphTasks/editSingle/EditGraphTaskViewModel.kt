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
import com.sharc.ramdhd.data.repository.GraphTaskRepository
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
    private var gratificationStepIndices = mutableSetOf<Int>()

    init {
        Log.d(TAG, "Initializing EditGraphTaskViewModel")
        val database = AppDatabase.getDatabase(application)
        repository = GraphTaskRepository(database.graphTaskDao())
    }

    fun getSteps(): LiveData<MutableList<String>> = _steps

    fun initializeSteps(steps: Array<String>?, gratificationSteps: IntArray? = null) {
        Log.d(TAG, "Initializing steps: ${steps?.joinToString()}")
        Log.d(TAG, "Initializing gratification steps: ${gratificationSteps?.joinToString()}")

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

        // Initialize gratification states from passed data
        gratificationStepIndices.clear()
        gratificationSteps?.forEach { index ->
            gratificationStepIndices.add(index)
            Log.d(TAG, "Added gratification step at index: $index")
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
        } catch (e: Exception) {
            Log.e(TAG, "Error updating step: ${e.message}", e)
        }
    }

    fun updateStepGratification(index: Int, isGratification: Boolean) {
        if (isGratification) {
            gratificationStepIndices.add(index)
            Log.d(TAG, "Marked step $index as gratification step")
        } else {
            gratificationStepIndices.remove(index)
            Log.d(TAG, "Removed gratification mark from step $index")
        }
        Log.d(TAG, "Current gratification steps: $gratificationStepIndices")
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
                    isFinishing = index == lastWrittenIndex,
                    isGratification = gratificationStepIndices.contains(index)
                )
            }

            // Validate minimum steps requirement
            if (stepsToSave.size < MIN_STEPS) {
                throw IllegalStateException("At least $MIN_STEPS steps are required")
            }

            Log.d(TAG, """Steps to save: 
                ${stepsToSave.joinToString("\n") {
                "- ${it.description} (Finishing: ${it.isFinishing}, Gratification: ${it.isGratification})"
            }}""".trimIndent())

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
                    Steps: ${result.steps.joinToString("\n") {
                    "- ${it.description} (Finishing: ${it.isFinishing}, Gratification: ${it.isGratification})"
                }}""".trimIndent())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving graph task: ${e.message}", e)
            throw e
        }
    }

    fun setTaskId(id: Int) {
        this.taskId = id
        Log.d(TAG, "Set task ID to: $id")
    }
}