package com.sharc.ramdhd.ui.dashboard.graphTasks.viewSingle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sharc.ramdhd.data.repository.GraphTaskRepository

class ViewSingleGraphTaskViewModelFactory(
    private val repository: GraphTaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewSingleGraphTaskViewModel::class.java)) {
            return ViewSingleGraphTaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}