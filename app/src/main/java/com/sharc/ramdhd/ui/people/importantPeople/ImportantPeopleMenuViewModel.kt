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

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ImportantPeopleRepository(database.importantEventDao())
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            repository.allEvents.collect { eventList ->
                _events.value = processEvents(eventList)
                _uniquePersonNames.value = eventList.map { it.personName }.distinct().sorted()
            }
        }
    }

    private fun processEvents(events: List<ImportantEvent>): List<EventListItem> {
        if (events.isEmpty()) return emptyList()

        return when (_currentFilter.value) {
            EventFilterType.BY_EVENT_TYPE -> groupByEventType(events)
            EventFilterType.BY_PERSON -> groupByPerson(events)
            EventFilterType.THIS_WEEK -> {
                val filteredEvents = filterThisWeekEvents(events)
                listOf(EventListItem.HeaderItem("This Week")) +
                        filteredEvents.map { EventListItem.EventItem(it) }
            }
            EventFilterType.THIS_MONTH -> {
                val filteredEvents = filterThisMonthEvents(events)
                listOf(EventListItem.HeaderItem("This Month")) +
                        filteredEvents.map { EventListItem.EventItem(it) }
            }
            else -> groupByEventType(events) // Default grouping
        }
    }

    private fun groupByEventType(events: List<ImportantEvent>): List<EventListItem> {
        return events.groupBy { it.eventType }
            .flatMap { (type, eventList) ->
                listOf(EventListItem.HeaderItem(type.toString().capitalize())) +
                        eventList.map { EventListItem.EventItem(it) }
            }
    }

    private fun groupByPerson(events: List<ImportantEvent>): List<EventListItem> {
        return events.groupBy { it.personName }
            .flatMap { (name, eventList) ->
                listOf(EventListItem.HeaderItem(name)) +
                        eventList.map { EventListItem.EventItem(it) }
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
        viewModelScope.launch {
            repository.getEventsForPerson(personName).collect { events ->
                _events.value = listOf(EventListItem.HeaderItem(personName)) +
                        events.map { EventListItem.EventItem(it) }
            }
        }
    }

    fun filterByEventType(eventType: EventType) {
        viewModelScope.launch {
            repository.allEvents.map { events ->
                events.filter { it.eventType == eventType }
            }.collect { filteredEvents ->
                _events.value = listOf(EventListItem.HeaderItem(eventType.toString().capitalize())) +
                        filteredEvents.map { EventListItem.EventItem(it) }
            }
        }
    }

    fun deleteEvents(events: List<ImportantEvent>) {
        viewModelScope.launch {
            repository.deleteEvents(events)
        }
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}