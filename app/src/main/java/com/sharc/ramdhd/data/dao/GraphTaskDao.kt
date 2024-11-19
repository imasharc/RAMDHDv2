package com.sharc.ramdhd.data.dao

import androidx.room.*
import com.sharc.ramdhd.data.model.graphTask.GraphTask
import com.sharc.ramdhd.data.model.graphTask.GraphStep
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface GraphTaskDao {
    @Transaction
    @Query("SELECT * FROM graph_tasks")
    fun getAllGraphTasksWithSteps(): Flow<List<GraphTaskWithSteps>>

    @Transaction
    @Query("SELECT * FROM graph_tasks WHERE id = :taskId")
    suspend fun getGraphTaskWithStepsOnce(taskId: Int): GraphTaskWithSteps?

    @Query("SELECT COUNT(*) FROM graph_steps WHERE taskId = :taskId AND isCompleted = 1")
    suspend fun getCompletedStepsCount(taskId: Int): Int

    @Query("SELECT COUNT(*) FROM graph_steps WHERE taskId = :taskId")
    suspend fun getTotalStepsCount(taskId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGraphTask(task: GraphTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<GraphStep>)

    @Transaction
    suspend fun insertGraphTaskWithSteps(task: GraphTask, steps: List<GraphStep>) {
        val taskId = insertGraphTask(task)
        val stepEntities = steps.map { it.copy(taskId = taskId.toInt()) }
        insertSteps(stepEntities)
    }

    @Update
    suspend fun updateGraphTask(task: GraphTask)

    @Transaction
    suspend fun updateGraphTaskWithSteps(task: GraphTask, newSteps: List<GraphStep>) {
        updateGraphTask(task)
        deleteStepsForTask(task.id)
        insertSteps(newSteps)
    }

    @Query("DELETE FROM graph_steps WHERE taskId = :taskId")
    suspend fun deleteStepsForTask(taskId: Int)

    @Delete
    suspend fun deleteGraphTasks(tasks: List<GraphTask>)

    // Add new method for updating step icon
    @Query("UPDATE graph_steps SET icon = :icon WHERE id = :stepId")
    suspend fun updateStepIcon(stepId: Int, icon: String)

    // Add method to get a single step
    @Query("SELECT * FROM graph_steps WHERE id = :stepId")
    suspend fun getStep(stepId: Int): GraphStep?

    @Query("UPDATE graph_steps SET isCompleted = :isCompleted WHERE id = :stepId")
    suspend fun updateStepCompletion(stepId: Int, isCompleted: Boolean)

    @Query("UPDATE graph_steps SET isCompleted = 0 WHERE taskId = :taskId")
    suspend fun resetTaskSteps(taskId: Int)

    @Query("UPDATE graph_tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Int)

    @Query("UPDATE graph_tasks SET isCompleted = 0 WHERE id = :taskId")
    suspend fun markTaskAsNotCompleted(taskId: Int)

    @Query("UPDATE graph_steps SET isGratification = :isGratification WHERE id = :stepId")
    suspend fun updateStepGratification(stepId: Int, isGratification: Boolean)
}