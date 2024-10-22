package com.sharc.ramdhd.ui.home.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.data.model.Note
import com.sharc.ramdhd.databinding.ItemNoteBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        private val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        fun bind(note: Note) {
            binding.textViewNoteTitle.text = note.title
            try {
                val dateTime = LocalDateTime.parse(note.timestamp.toString(), inputFormatter)
                binding.textViewNoteDate.text = dateTime.format(outputFormatter)
            } catch (e: Exception) {
                binding.textViewNoteDate.text = note.timestamp.toString()
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}