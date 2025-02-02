package com.sharc.ramdhd.ui.home.notes.editSingle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.Note
import com.sharc.ramdhd.data.repository.NoteRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class EditNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    private var currentNoteId: Int = -1

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao, application)
    }

    fun setNoteId(id: Int) {
        currentNoteId = id
    }

    fun saveNote(title: String, description: String) {
        viewModelScope.launch {
            val warsawTime = LocalDateTime.now(ZoneId.of("Europe/Warsaw"))
            val note = if (currentNoteId != -1) {
                Note(
                    id = currentNoteId,
                    title = title,
                    description = description,
                    timestamp = warsawTime
                )
            } else {
                Note(
                    title = title,
                    description = description,
                    timestamp = warsawTime
                )
            }
            repository.insert(note)
        }
    }
}