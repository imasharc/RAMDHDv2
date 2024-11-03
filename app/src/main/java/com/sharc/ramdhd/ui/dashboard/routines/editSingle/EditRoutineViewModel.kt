package com.sharc.ramdhd.ui.dashboard.routines.editSingle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class EditRoutineViewModel : ViewModel() {
    private val _steps = MutableLiveData<MutableList<String>>()

    init {
        // Initialize with 3 empty steps
        _steps.value = MutableList(3) { "" }
    }

    fun updateStep(index: Int, text: String) {
        _steps.value?.let { currentSteps ->
            // Ensure the list is large enough
            while (index >= currentSteps.size) {
                currentSteps.add("")
            }
            currentSteps[index] = text
            _steps.value = currentSteps
        }
    }

    fun getRoutineData(title: String, description: String): String {
        val steps = _steps.value!!

        // Find the last index where any step has content
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
            // Take all steps up to lastWrittenIndex, including empty ones
            for (i in 0..lastWrittenIndex) {
                append("${i + 1}. ${steps[i]}\n")
            }
        }
    }
}