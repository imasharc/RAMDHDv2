package com.sharc.ramdhd.ui.people.importantPeople

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.importantPeople.EventType
import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent
import com.sharc.ramdhd.data.repository.ImportantPeopleRepository
import com.sharc.ramdhd.ui.people.importantPeople.EventListItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ImportantPeopleMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ImportantPeopleRepository

    private val _events = MutableLiveData<List<EventListItem>>()
    val events: LiveData<List<EventListItem>> = _events

    private val _uniquePersonNames = MutableLiveData<List<String>>()
    val uniquePersonNames: LiveData<List<String>> = _uniquePersonNames

    private val _currentFilter = MutableLiveData(EventFilterType.ALL)
    val currentFilter: LiveData<EventFilterType> = _currentFilter

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ImportantPeopleRepository(database.importantEventDao())
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            repository.allEvents.collect { eventList ->
                val trimmedEvents = eventList.map { it.copy(personName = it.personName.trim()) }
                _events.value = processEvents(trimmedEvents)
                _uniquePersonNames.value = trimmedEvents.map { it.personName }.distinct().sorted()
            }
        }
    }

    private fun processEvents(events: List<ImportantEvent>): List<EventListItem> {
        if (events.isEmpty()) return emptyList()

        val sortedEvents = when (_currentFilter.value) {
            EventFilterType.BY_EVENT_TYPE -> events.sortedBy { it.eventType.name }
            EventFilterType.BY_PERSON -> events.sortedBy { it.personName.trim() }
            EventFilterType.THIS_WEEK -> filterThisWeekEvents(events).sortedBy { it.eventDate }
            EventFilterType.THIS_MONTH -> filterThisMonthEvents(events).sortedBy { it.eventDate }
            else -> events.sortedBy { it.eventDate }
        }

        return sortedEvents.fold(mutableListOf()) { acc, event ->
            if (acc.isEmpty() || shouldAddDelimiter(acc.last(), event)) {
                acc.add(EventListItem.HeaderItem(getHeaderText(event)))
            }
            acc.add(EventListItem.EventItem(event))
            acc
        }
    }

    private fun shouldAddDelimiter(lastItem: EventListItem, newEvent: ImportantEvent): Boolean {
        val lastEvent = (lastItem as? EventListItem.EventItem)?.event ?: return false
        return when (_currentFilter.value) {
            EventFilterType.BY_EVENT_TYPE -> lastEvent.eventType != newEvent.eventType
            EventFilterType.BY_PERSON -> lastEvent.personName.trim() != newEvent.personName.trim()
            else -> false
        }
    }

    private fun getHeaderText(event: ImportantEvent): String {
        return when (_currentFilter.value) {
            EventFilterType.BY_EVENT_TYPE -> event.eventType.toString().lowercase().capitalize()
            EventFilterType.BY_PERSON -> event.personName.trim()
            EventFilterType.THIS_WEEK -> "This Week"
            EventFilterType.THIS_MONTH -> "This Month"
            else -> ""
        }
    }

    private fun filterThisWeekEvents(events: List<ImportantEvent>): List<ImportantEvent> {
        val now = LocalDateTime.now()
        val weekStart = now.truncatedTo(ChronoUnit.DAYS)
        val weekEnd = weekStart.plusWeeks(1)
        return events.filter { event ->
            event.eventDate.isAfter(weekStart) && event.eventDate.isBefore(weekEnd)
        }
    }

    private fun filterThisMonthEvents(events: List<ImportantEvent>): List<ImportantEvent> {
        val now = LocalDateTime.now()
        val monthStart = now.withDayOfMonth(1)
        val monthEnd = monthStart.plusMonths(1)
        return events.filter { event ->
            event.eventDate.isAfter(monthStart) && event.eventDate.isBefore(monthEnd)
        }
    }

    fun setFilter(filterType: EventFilterType) {
        _currentFilter.value = filterType
        loadEvents()
    }

    fun filterByPerson(personName: String) {
        val trimmedName = personName.trim()
        viewModelScope.launch {
            repository.allEvents
                .map { events ->
                    events.filter { it.personName.trim().equals(trimmedName, ignoreCase = true) }
                        .sortedBy { it.eventDate }
                }
                .collect { filteredEvents ->
                    _events.value = filteredEvents.map { EventListItem.EventItem(it) }
                }
        }
    }

    fun filterByEventType(eventType: EventType) {
        viewModelScope.launch {
            repository.allEvents
                .map { events ->
                    events.filter { it.eventType == eventType }
                        .sortedBy { it.eventDate }
                }
                .collect { filteredEvents ->
                    _events.value = filteredEvents.map { EventListItem.EventItem(it) }
                }
        }
    }

    fun deleteEvents(events: List<ImportantEvent>) {
        viewModelScope.launch {
            try {
                repository.deleteEvents(events)
                _deleteResult.value = true
                loadEvents() // Reload events after successful deletion
            } catch (e: Exception) {
                _deleteResult.value = false
            }
        }
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}