package com.sharc.ramdhd.data.repository

import com.sharc.ramdhd.data.dao.GraphTaskDao
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


    // Add new method for updating step icon
    suspend fun updateStepIcon(stepId: Int, icon: String) {
        graphTaskDao.updateStepIcon(stepId, icon)
    }

    suspend fun updateStepCompletion(stepId: Int, isCompleted: Boolean) {
        graphTaskDao.updateStepCompletion(stepId, isCompleted)
        // Get the step to find its taskId
        graphTaskDao.getStep(stepId)?.let { step ->
            // Check if all steps are completed
            if (areAllStepsCompleted(step.taskId)) {
                // If all steps are completed, mark the task as completed
                graphTaskDao.markTaskAsCompleted(step.taskId)
            } else {
                // If not all steps are completed, mark the task as not completed
                graphTaskDao.markTaskAsNotCompleted(step.taskId)
            }
        }
    }

    suspend fun areAllStepsCompleted(taskId: Int): Boolean {
        val completedCount = graphTaskDao.getCompletedStepsCount(taskId)
        val totalCount = graphTaskDao.getTotalStepsCount(taskId)
        return completedCount == totalCount && totalCount > 0
    }

    suspend fun resetTask(taskId: Int) {
        graphTaskDao.resetTaskSteps(taskId)
        graphTaskDao.markTaskAsNotCompleted(taskId)
    }

    suspend fun markTaskAsCompleted(taskId: Int) {
        graphTaskDao.markTaskAsCompleted(taskId)
    }

    suspend fun markTaskAsNotCompleted(taskId: Int) {
        graphTaskDao.markTaskAsNotCompleted(taskId)
    }

    // Add method to get a single step
    suspend fun getStep(stepId: Int): GraphStep? {
        return graphTaskDao.getStep(stepId)
    }
}