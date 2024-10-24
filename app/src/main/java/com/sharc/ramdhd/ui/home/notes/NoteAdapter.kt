package com.sharc.ramdhd.ui.home.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.data.model.Note
import com.sharc.ramdhd.databinding.ItemNoteBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {
    private var selectionMode = false
    private val selectedNotes = mutableSetOf<Note>()

    private var onItemLongClickListener: ((Note) -> Unit)? = null
    private var onSelectionChanged: ((Int) -> Unit)? = null

    fun setOnItemLongClickListener(listener: (Note) -> Unit) {
        onItemLongClickListener = listener
    }

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChanged = listener
    }

    fun toggleSelectionMode() {
        selectionMode = !selectionMode
        if (!selectionMode) {
            selectedNotes.clear()
        }
        notifyDataSetChanged()
    }

    fun selectAllNotes() {
        selectedNotes.clear()
        currentList.forEach { note ->
            selectedNotes.add(note)
        }
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedNotes.size)
    }

    fun deselectAllNotes() {
        selectedNotes.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }

    fun isAllSelected(): Boolean = selectedNotes.size == currentList.size

    fun isInSelectionMode() = selectionMode

    fun getSelectedNotes(): Set<Note> = selectedNotes.toSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, selectionMode, selectedNotes.contains(note))

        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                onItemLongClickListener?.invoke(note)
                toggleSelectionMode()
                selectedNotes.add(note)
                notifyDataSetChanged()
                onSelectionChanged?.invoke(selectedNotes.size)
            }
            true
        }

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                if (selectedNotes.contains(note)) {
                    selectedNotes.remove(note)
                } else {
                    selectedNotes.add(note)
                }
                notifyItemChanged(position)
                onSelectionChanged?.invoke(selectedNotes.size)
            }
        }
    }

    class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        private val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        fun bind(note: Note, isSelectionMode: Boolean, isSelected: Boolean) {
            binding.apply {
                textViewNoteTitle.text = note.title
                try {
                    val dateTime = LocalDateTime.parse(note.timestamp.toString(), inputFormatter)
                    textViewNoteDate.text = dateTime.format(outputFormatter)
                } catch (e: Exception) {
                    textViewNoteDate.text = note.timestamp.toString()
                }

                // Smooth checkbox animation
                if (isSelectionMode) {
                    checkBoxNote.alpha = 0f
                    checkBoxNote.visibility = View.VISIBLE
                    checkBoxNote.animate()
                        .alpha(1f)
                        .setDuration(50)
                        .start()
                } else {
                    checkBoxNote.animate()
                        .alpha(0f)
                        .setDuration(50)
                        .withEndAction {
                            checkBoxNote.visibility = View.GONE
                        }
                        .start()
                }

                // Update checkbox state
                checkBoxNote.isChecked = isSelected
                root.isActivated = isSelected
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