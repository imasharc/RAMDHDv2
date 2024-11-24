package com.sharc.ramdhd.ui.people.importantPeople.editSingle

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.ImportantEvent
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EditSingleImportantEventViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ImportantEventVM"
    private val database = AppDatabase.getDatabase(application)
    private val importantEventDao = database.importantEventDao()

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
            val event = ImportantEvent(
                personName = personName,
                eventTitle = eventTitle,
                eventDate = _selectedDate.value!!,
                description = description
            )

            try {
                importantEventDao.insert(event)

                // Log the saved event details
                Log.d(TAG, """
                    Event saved successfully:
                    Person: $personName
                    Title: $eventTitle
                    Date: ${event.eventDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
                    Description: $description
                """.trimIndent())

                // Log all events in the database using the new suspend function
                val allEvents = importantEventDao.getAllEventsList()
                Log.d(TAG, "All events in database (${allEvents.size} total):")
                allEvents.forEach { savedEvent ->
                    Log.d(TAG, """
                        ID: ${savedEvent.id}
                        Person: ${savedEvent.personName}
                        Title: ${savedEvent.eventTitle}
                        Date: ${savedEvent.eventDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
                        Description: ${savedEvent.description}
                        ------------------------
                    """.trimIndent())
                }

                _saveStatus.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving event", e)
                _saveStatus.value = false
            }
        }
    }
}