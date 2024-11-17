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
}