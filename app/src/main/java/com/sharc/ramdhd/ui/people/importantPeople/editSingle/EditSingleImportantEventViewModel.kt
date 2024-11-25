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

    private val _selectedDate = MutableLiveData<LocalDateTime>()
    val selectedDate: LiveData<LocalDateTime> = _selectedDate

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _event = MutableLiveData<ImportantEvent>()
    val event: LiveData<ImportantEvent> = _event

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ImportantPeopleRepository(database.importantEventDao())
    }

    fun loadEvent(eventId: Int) {
        if (eventId != -1) {
            viewModelScope.launch {
                try {
                    repository.getEventById(eventId)?.let { event ->
                        _event.value = event
                        _selectedDate.value = event.eventDate
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading event", e)
                }
            }
        }
    }

    fun setSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        _selectedDate.value = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
        Log.d(TAG, "Selected date set to: ${_selectedDate.value}")
    }

    fun saveEvent(
        eventId: Int = -1,
        personName: String,
        eventType: EventType,
        eventName: String,
        recurrenceType: RecurrenceType,
        description: String
    ) {
        if (personName.isBlank() || eventName.isBlank() || _selectedDate.value == null) {
            Log.e(TAG, """
                Validation failed:
                personName: $personName
                eventName: $eventName
                date: ${_selectedDate.value}
            """.trimIndent())
            _saveStatus.value = false
            return
        }

        viewModelScope.launch {
            try {
                val event = ImportantEvent(
                    id = if (eventId == -1) 0 else eventId,
                    personName = personName.trim(),
                    eventType = eventType,
                    eventName = eventName.trim(),
                    eventDate = _selectedDate.value!!,
                    recurrenceType = recurrenceType,
                    description = description.trim()
                )

                if (eventId == -1) {
                    repository.insert(event)
                    Log.d(TAG, "New event saved: $event")
                } else {
                    repository.update(event)
                    Log.d(TAG, "Event updated: $event")
                }

                _saveStatus.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving event", e)
                _saveStatus.value = false
            }
        }
    }

    fun getExistingEvent(eventId: Int): ImportantEvent? {
        return _event.value
    }

    fun isExistingEvent(eventId: Int): Boolean {
        return eventId != -1
    }
}