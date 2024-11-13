package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import com.sharc.ramdhd.databinding.ItemNoteBinding

class GraphTaskAdapter : ListAdapter<GraphTaskWithSteps, GraphTaskAdapter.GraphTaskViewHolder>(GraphTaskDiffCallback()) {
    private var selectionMode = false
    private val selectedTasks = mutableSetOf<GraphTaskWithSteps>()
    private var onItemClickListener: ((GraphTaskWithSteps) -> Unit)? = null
    private var onItemLongClickListener: ((GraphTaskWithSteps) -> Unit)? = null
    private var onSelectionChanged: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (GraphTaskWithSteps) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (GraphTaskWithSteps) -> Unit) {
        onItemLongClickListener = listener
    }

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChanged = listener
    }

    fun toggleSelectionMode() {
        selectionMode = !selectionMode
        if (!selectionMode) {
            selectedTasks.clear()
            onSelectionChanged?.invoke(0) // Ensure UI updates when exiting selection mode
        }
        notifyDataSetChanged()
    }

    fun selectAllTasks() {
        selectedTasks.clear()
        currentList.forEach { task ->
            selectedTasks.add(task)
        }
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedTasks.size)
    }

    fun deselectAllTasks() {
        selectedTasks.clear()
        selectionMode = false // Exit selection mode when deselecting all
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }

    fun isAllSelected(): Boolean = selectedTasks.size == currentList.size

    fun isInSelectionMode() = selectionMode

    fun getSelectedTasks(): Set<GraphTaskWithSteps> = selectedTasks.toSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GraphTaskViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GraphTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GraphTaskViewHolder, position: Int) {
        val graphTask = getItem(position)
        holder.bind(graphTask, selectionMode, selectedTasks.contains(graphTask))

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                if (selectedTasks.contains(graphTask)) {
                    selectedTasks.remove(graphTask)
                    // Exit selection mode if no items are selected
                    if (selectedTasks.isEmpty()) {
                        selectionMode = false
                        onSelectionChanged?.invoke(0)
                        notifyDataSetChanged()
                    } else {
                        notifyItemChanged(position)
                        onSelectionChanged?.invoke(selectedTasks.size)
                    }
                } else {
                    selectedTasks.add(graphTask)
                    notifyItemChanged(position)
                    onSelectionChanged?.invoke(selectedTasks.size)
                }
            } else {
                onItemClickListener?.invoke(graphTask)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                onItemLongClickListener?.invoke(graphTask)
                selectionMode = true
                selectedTasks.add(graphTask)
                notifyDataSetChanged()
                onSelectionChanged?.invoke(selectedTasks.size)
            }
            true
        }
    }

    class GraphTaskViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(graphTask: GraphTaskWithSteps, isSelectionMode: Boolean, isSelected: Boolean) {
            binding.apply {
                textViewNoteTitle.text = graphTask.task.title
                // Show number of steps as the subtitle
                textViewNoteDate.text = "${graphTask.steps.size} steps"

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

                // Disable checkbox's own click listener
                checkBoxNote.isClickable = false
                checkBoxNote.isFocusable = false
            }
        }
    }

    class GraphTaskDiffCallback : DiffUtil.ItemCallback<GraphTaskWithSteps>() {
        override fun areItemsTheSame(
            oldItem: GraphTaskWithSteps,
            newItem: GraphTaskWithSteps
        ): Boolean {
            return oldItem.task.id == newItem.task.id
        }

        override fun areContentsTheSame(
            oldItem: GraphTaskWithSteps,
            newItem: GraphTaskWithSteps
        ): Boolean {
            return oldItem == newItem
        }
    }
}