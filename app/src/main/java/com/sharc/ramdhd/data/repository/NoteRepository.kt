package com.sharc.ramdhd.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.sharc.ramdhd.data.dao.NoteDao
import com.sharc.ramdhd.data.model.Note
import com.sharc.ramdhd.service.NoteService
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val context: Context
) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    suspend fun insert(note: Note) {
        val id = noteDao.insertNote(note)

        // Direct service call instead of using NotificationHelper
        val serviceIntent = Intent(context, NoteService::class.java).apply {
            putExtra("note_id", id.toInt())
            putExtra("note_title", note.title)
            putExtra("note_description", note.description)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    suspend fun delete(notes: List<Note>) {
        notes.forEach { note ->
            // Direct service call to cancel notification
            val serviceIntent = Intent(context, NoteService::class.java).apply {
                action = NoteService.ACTION_STOP_NOTE
                putExtra("note_id", note.id)
            }
            context.startService(serviceIntent)
        }
        noteDao.deleteNotes(notes)
    }
}