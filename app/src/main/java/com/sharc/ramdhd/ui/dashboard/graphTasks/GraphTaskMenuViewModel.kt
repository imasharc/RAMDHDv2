package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.repository.GraphTaskRepository
import kotlinx.coroutines.flow.Flow
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import kotlinx.coroutines.launch

class GraphTaskMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GraphTaskRepository
    val allGraphTasks: Flow<List<GraphTaskWithSteps>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GraphTaskRepository(database.graphTaskDao())
        allGraphTasks = repository.allGraphTasks
    }

    // Update to handle List<GraphTask>
    suspend fun deleteGraphTasks(tasks: Set<GraphTaskWithSteps>) {
        // Convert Set<GraphTaskWithSteps> to List<GraphTask>
        val graphTasks = tasks.map { it.task }
        repository.delete(graphTasks)
    }
}