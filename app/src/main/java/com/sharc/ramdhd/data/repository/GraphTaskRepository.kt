package com.sharc.ramdhd.data.repository.graphTask

import com.sharc.ramdhd.data.dao.graphTask.GraphTaskDao
import com.sharc.ramdhd.data.model.graphTask.GraphTask
import com.sharc.ramdhd.data.model.graphTask.GraphStep
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import kotlinx.coroutines.flow.Flow

class GraphTaskRepository(private val graphTaskDao: GraphTaskDao) {
    val allGraphTasks: Flow<List<GraphTaskWithSteps>> = graphTaskDao.getAllGraphTasksWithSteps()

    suspend fun getGraphTaskWithStepsOnce(taskId: Int): GraphTaskWithSteps? {
        return graphTaskDao.getGraphTaskWithStepsOnce(taskId)
    }

    suspend fun insert(task: GraphTask, steps: List<GraphStep>) {
        graphTaskDao.insertGraphTaskWithSteps(task, steps)
    }

    suspend fun updateGraphTaskWithSteps(task: GraphTask, steps: List<GraphStep>) {
        graphTaskDao.updateGraphTaskWithSteps(task, steps)
    }

    suspend fun delete(tasks: List<GraphTask>) {
        graphTaskDao.deleteGraphTasks(tasks)
    }
}