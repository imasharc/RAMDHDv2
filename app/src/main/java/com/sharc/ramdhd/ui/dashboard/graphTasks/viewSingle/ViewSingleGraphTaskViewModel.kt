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
}