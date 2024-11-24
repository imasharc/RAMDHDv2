package com.sharc.ramdhd.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sharc.ramdhd.data.model.ImportantEvent

@Dao
interface ImportantEventDao {
    @Query("SELECT * FROM important_events ORDER BY eventDate ASC")
    fun getAllEvents(): LiveData<List<ImportantEvent>>

    @Query("SELECT * FROM important_events ORDER BY eventDate ASC")
    suspend fun getAllEventsList(): List<ImportantEvent>

    @Insert
    suspend fun insert(event: ImportantEvent)

    @Update
    suspend fun update(event: ImportantEvent)

    @Delete
    suspend fun delete(event: ImportantEvent)

    @Query("SELECT * FROM important_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Int): ImportantEvent?
}