package com.sharc.ramdhd.data.repository

import com.sharc.ramdhd.data.dao.ImportantEventDao
import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class ImportantPeopleRepository(private val importantEventDao: ImportantEventDao) {
    // Get all events as Flow for continuous updates
    val allEvents: Flow<List<ImportantEvent>> = importantEventDao.getAllEventsFlow()

    // Get upcoming events (from today onwards)
    val upcomingEvents: Flow<List<ImportantEvent>> = importantEventDao.getAllEventsFlow().map { events ->
        events.filter { event ->
            event.eventDate.isAfter(LocalDateTime.now().minusDays(1))
        }.sortedBy { it.eventDate }
    }

    // Get events for a specific person
    fun getEventsForPerson(personName: String): Flow<List<ImportantEvent>> {
        return importantEventDao.getEventsForPerson(personName)
    }

    // Get a single event by ID
    suspend fun getEventById(eventId: Int): ImportantEvent? {
        return importantEventDao.getEventById(eventId)
    }

    // Insert new event
    suspend fun insert(event: ImportantEvent) {
        importantEventDao.insert(event)
    }

    // Update existing event
    suspend fun update(event: ImportantEvent) {
        importantEventDao.update(event)
    }

    // Delete event
    suspend fun delete(event: ImportantEvent) {
        importantEventDao.delete(event)
    }

    // Delete multiple events
    suspend fun deleteEvents(events: List<ImportantEvent>) {
        importantEventDao.deleteEvents(events)
    }

    // Get events for a specific date
    suspend fun getEventsForDate(date: LocalDateTime): List<ImportantEvent> {
        return importantEventDao.getEventsForDate(date)
    }

    // Get events between two dates
    suspend fun getEventsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): List<ImportantEvent> {
        return importantEventDao.getEventsBetweenDates(startDate, endDate)
    }
}