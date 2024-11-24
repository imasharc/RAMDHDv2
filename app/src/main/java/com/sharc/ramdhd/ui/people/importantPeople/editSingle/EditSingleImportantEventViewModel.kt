package com.sharc.ramdhd.ui.people.importantPeople.editSingle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.ImportantEvent
import com.sharc.ramdhd.data.repository.ImportantPeopleRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        Log.d(TAG, "Selected date: ${_selectedDate.value?.format(DateTimeFormatter.ISO_DATE)}")
    }

    fun saveEvent(personName: String, eventTitle: String, description: String) {
        if (personName.isBlank() || eventTitle.isBlank() || _selectedDate.value == null) {
            Log.e(TAG, "Validation failed: personName: $personName, eventTitle: $eventTitle, date: ${_selectedDate.value}")
            _saveStatus.value = false
            return
        }

        viewModelScope.launch {
            try {
                val event = ImportantEvent(
                    personName = personName,
                    eventTitle = eventTitle,
                    eventDate = _selectedDate.value!!,
                    description = description
                )

                repository.insert(event)

                Log.d(TAG, """
                    Event saved successfully:
                    Person: $personName
                    Title: $eventTitle
                    Date: ${event.eventDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
                    Description: $description
                """.trimIndent())

                _saveStatus.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving event", e)
                _saveStatus.value = false
            }
        }
    }
}