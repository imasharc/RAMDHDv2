package com.sharc.ramdhd.ui.dashboard.routines.viewSingle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sharc.ramdhd.data.repository.RoutineRepository

class ViewSingleRoutineViewModelFactory(
    private val repository: RoutineRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewSingleRoutineViewModel::class.java)) {
            return ViewSingleRoutineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}