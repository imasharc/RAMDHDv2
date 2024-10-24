package com.sharc.ramdhd.data.repository

import com.sharc.ramdhd.data.dao.NoteDao
import com.sharc.ramdhd.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun update(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun delete(notes: List<Note>) {
        noteDao.deleteNotes(notes)
    }
}