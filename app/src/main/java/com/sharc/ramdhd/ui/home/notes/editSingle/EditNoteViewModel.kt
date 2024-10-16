package com.sharc.ramdhd.ui.home.notes.editSingle

import android.util.Log
import androidx.lifecycle.ViewModel

class EditNoteViewModel : ViewModel() {
    fun saveNote(title: String, description: String) {
        Log.d("EditNoteViewModel", "Title: $title")
        Log.d("EditNoteViewModel", "Description: $description")
    }
}
