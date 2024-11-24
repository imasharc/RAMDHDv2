package com.sharc.ramdhd.data.dao

import androidx.room.*
import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ImportantEventDao {
    @Query("SELECT * FROM important_events ORDER BY eventDate ASC")
    fun getAllEventsFlow(): Flow<List<ImportantEvent>>

    @Query("SELECT * FROM important_events WHERE personName = :personName ORDER BY eventDate ASC")
    fun getEventsForPerson(personName: String): Flow<List<ImportantEvent>>

    @Query("SELECT * FROM important_events WHERE date(eventDate) = date(:date) ORDER BY eventDate ASC")
    suspend fun getEventsForDate(date: LocalDateTime): List<ImportantEvent>

    @Query("SELECT * FROM important_events WHERE eventDate BETWEEN :startDate AND :endDate ORDER BY eventDate ASC")
    suspend fun getEventsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): List<ImportantEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ImportantEvent)

    @Update
    suspend fun update(event: ImportantEvent)

    @Delete
    suspend fun delete(event: ImportantEvent)

    @Delete
    suspend fun deleteEvents(events: List<ImportantEvent>)

    @Query("SELECT * FROM important_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Int): ImportantEvent?
}