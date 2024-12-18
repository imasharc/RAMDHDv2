package com.sharc.ramdhd.ui.dashboard.graphTasks.viewSingle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import com.sharc.ramdhd.data.repository.GraphTaskRepository
import kotlinx.coroutines.launch

class ViewSingleGraphTaskViewModel(private val repository: GraphTaskRepository) : ViewModel() {
    companion object {
        private const val TAG = "ViewSingleGraphTaskVM"
    }

    private val _graphTask = MutableLiveData<GraphTaskWithSteps?>()
    val graphTask: LiveData<GraphTaskWithSteps?> = _graphTask

    fun loadGraphTask(taskId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading graph task with ID: $taskId")
                val task = repository.getGraphTaskWithStepsOnce(taskId)
                if (task != null) {
                    Log.d(TAG, """
                        Loaded graph task:
                        Title: ${task.task.title}
                        Description: ${task.task.description}
                        Steps count: ${task.steps.size}
                        Steps details: ${task.steps.joinToString("\n") {
                        "- ${it.description} (Gratification: ${it.isGratification}, Completed: ${it.isCompleted})"
                    }}
                    """.trimIndent())
                    _graphTask.value = task
                } else {
                    Log.w(TAG, "No graph task found with ID: $taskId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading graph task: ${e.message}", e)
            }
        }
    }

    fun handleStepStateChange(
        stepId: Int,
        isCompleted: Boolean,
        taskId: Int,
        onCompletion: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating step $stepId completion state to: $isCompleted")
                repository.updateStepCompletion(stepId, isCompleted)

                // Check if all steps are completed after updating
                val allCompleted = repository.areAllStepsCompleted(taskId)
                Log.d(TAG, "All steps completed: $allCompleted")

                // Reload task data to refresh the UI
                loadGraphTask(taskId)

                // Notify about completion status
                onCompletion(allCompleted)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling step state change: ${e.message}", e)
            }
        }
    }

    fun updateStepIcon(stepId: Int, icon: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating step $stepId icon to: $icon")
                repository.updateStepIcon(stepId, icon)

                // Reload the task to refresh the UI
                _graphTask.value?.task?.id?.let { taskId ->
                    loadGraphTask(taskId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating step icon: ${e.message}", e)
            }
        }
    }

    fun updateStepCompletion(stepId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating step $stepId completion state to: $isCompleted")
                repository.updateStepCompletion(stepId, isCompleted)

                // Also check if the step is a gratification step
                val step = repository.getStep(stepId)
                if (step?.isGratification == true) {
                    Log.d(TAG, "Updated gratification step $stepId completion state")
                }

                // Reload to refresh the UI
                _graphTask.value?.task?.id?.let { taskId ->
                    loadGraphTask(taskId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating step completion: ${e.message}", e)
            }
        }
    }

    fun resetTask(taskId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Resetting task $taskId")
                repository.resetTask(taskId)

                // Reload to show reset state
                loadGraphTask(taskId)
                Log.d(TAG, "Task reset completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting task: ${e.message}", e)
            }
        }
    }

    fun updateStepGratification(stepId: Int, isGratification: Boolean) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating step $stepId gratification state to: $isGratification")
                repository.updateStepGratification(stepId, isGratification)

                // Reload to refresh the UI
                _graphTask.value?.task?.id?.let { taskId ->
                    loadGraphTask(taskId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating step gratification: ${e.message}", e)
            }
        }
    }
}