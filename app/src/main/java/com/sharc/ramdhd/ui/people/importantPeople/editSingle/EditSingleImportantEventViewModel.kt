package com.sharc.ramdhd.ui.people.importantPeople.editSingle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.importantPeople.EventType
import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent
import com.sharc.ramdhd.data.model.importantPeople.RecurrenceType
import com.sharc.ramdhd.data.repository.ImportantPeopleRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EditSingleImportantEventViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ImportantEventVM"
    private val repository: ImportantPeopleRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ImportantPeopleRepository(database.importantEventDao())
    }

    private val _selectedDate = MutableLiveData<LocalDateTime>()
    val selectedDate: LiveData<LocalDateTime> = _selectedDate

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    fun setSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        _selectedDate.value = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
    }

    fun saveEvent(
        personName: String,
        eventType: EventType,
        eventName: String,
        recurrenceType: RecurrenceType,
        description: String
    ) {
        if (personName.isBlank() || eventName.isBlank() || _selectedDate.value == null) {
            _saveStatus.value = false
            return
        }

        viewModelScope.launch {
            try {
                val event = ImportantEvent(
                    personName = personName,
                    eventType = eventType,
                    eventName = eventName,
                    eventDate = _selectedDate.value!!,
                    recurrenceType = recurrenceType,
                    description = description
                )

                repository.insert(event)
                Log.d(TAG, "Event saved successfully: $event")
                _saveStatus.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving event", e)
                _saveStatus.value = false
            }
        }
    }
}