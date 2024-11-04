package com.sharc.ramdhd.ui.dashboard.routines.editSingle

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.Routine
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.data.model.Step
import com.sharc.ramdhd.data.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class EditRoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val _steps = MutableLiveData<MutableList<String>>()
    private val repository: RoutineRepository

    init {
        _steps.value = MutableList(3) { "" }
        val database = AppDatabase.getDatabase(getApplication<Application>())
        repository = RoutineRepository(database.routineDao())
    }

    fun updateStep(index: Int, text: String) {
        _steps.value?.let { currentSteps ->
            while (index >= currentSteps.size) {
                currentSteps.add("")
            }
            currentSteps[index] = text
            _steps.value = currentSteps
        }
    }

    fun getRoutineData(title: String, description: String): String {
        val steps = _steps.value!!
        var lastWrittenIndex = -1
        for (i in steps.indices) {
            if (steps[i].isNotEmpty()) {
                lastWrittenIndex = i
            }
        }

        return buildString {
            append("title: $title\n")
            append("description: $description\n")
            append("steps: \n")
            for (i in 0..lastWrittenIndex) {
                append("${i + 1}. ${steps[i]}\n")
            }
        }
    }

    // Add access to repository's allRoutines
    val allRoutines = repository.allRoutines

    suspend fun logAllRoutines() {
        allRoutines.first().let { routines ->
            Log.d(TAG, "All routines in database:")
            routines.forEach { routineWithSteps ->
                Log.d(TAG, "\nRoutine: " + buildString {
                    append("ID: ${routineWithSteps.routine.id}, ")
                    append("Title: ${routineWithSteps.routine.title}, ")
                    append("Description: ${routineWithSteps.routine.description}, ")
                    append("Timestamp: ${routineWithSteps.routine.timestamp}\n")
                    append("Steps:\n")
                    routineWithSteps.steps.forEachIndexed { index, step ->
                        append("${index + 1}. ${step.description}\n")
                    }
                })
            }
        }
    }

    suspend fun saveRoutine(title: String, description: String): RoutineWithSteps {
        val routine = Routine(
            title = title,
            description = description,
            timestamp = LocalDateTime.now(),
            isSelected = false
        )

        // Get steps up to the last written one
        val steps = _steps.value!!
        var lastWrittenIndex = -1
        for (i in steps.indices) {
            if (steps[i].isNotEmpty()) {
                lastWrittenIndex = i
            }
        }

        val relevantSteps = steps.subList(0, lastWrittenIndex + 1)

        // Save to database
        repository.insert(routine, relevantSteps.mapIndexed { index, content ->
            Step(
                routineId = routine.id,
                orderNumber = index,
                description = content
            )
        })

        return RoutineWithSteps(
            routine = routine,
            steps = relevantSteps.mapIndexed { index, content ->
                Step(
                    routineId = routine.id,
                    orderNumber = index,
                    description = content
                )
            }
        )
    }
}