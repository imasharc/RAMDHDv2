package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.repository.graphTask.GraphTaskRepository
import kotlinx.coroutines.flow.Flow
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps

class GraphTaskMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GraphTaskRepository
    val allGraphTasks: Flow<List<GraphTaskWithSteps>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GraphTaskRepository(database.graphTaskDao())
        allGraphTasks = repository.allGraphTasks
    }
}