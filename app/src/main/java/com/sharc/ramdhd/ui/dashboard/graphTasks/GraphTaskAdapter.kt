package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import com.sharc.ramdhd.databinding.ItemGraphTaskBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }

    fun isAllSelected(): Boolean = selectedTasks.size == currentList.size

    fun isInSelectionMode() = selectionMode

    fun getSelectedTasks(): Set<GraphTaskWithSteps> = selectedTasks.toSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GraphTaskViewHolder {
        val binding = ItemGraphTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GraphTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GraphTaskViewHolder, position: Int) {
        val graphTask = getItem(position)
        holder.bind(graphTask, selectionMode, selectedTasks.contains(graphTask))

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                if (selectedTasks.contains(graphTask)) {
                    selectedTasks.remove(graphTask)
                } else {
                    selectedTasks.add(graphTask)
                }
                notifyItemChanged(position)
                onSelectionChanged?.invoke(selectedTasks.size)
            } else {
                onItemClickListener?.invoke(graphTask)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                onItemLongClickListener?.invoke(graphTask)
                toggleSelectionMode()
                selectedTasks.add(graphTask)
                notifyDataSetChanged()
                onSelectionChanged?.invoke(selectedTasks.size)
            }
            true
        }
    }

    class GraphTaskViewHolder(private val binding: ItemGraphTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        private val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        fun bind(graphTask: GraphTaskWithSteps, isSelectionMode: Boolean, isSelected: Boolean) {
            binding.apply {
                textViewGraphTaskTitle.text = graphTask.task.title

                // Handle completed state
                if (graphTask.task.isCompleted) {
                    // Change the entire card background to green
                    root.setCardBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_green_light)
                    )
                    textViewGraphTaskTitle.setTextColor(
                        ContextCompat.getColor(root.context, android.R.color.black)
                    )
                    textViewGraphTaskDate.setTextColor(
                        ContextCompat.getColor(root.context, android.R.color.darker_gray)
                    )
                } else {
                    // Reset to default colors
                    root.setCardBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.white)
                    )
                    textViewGraphTaskTitle.setTextColor(
                        ContextCompat.getColor(root.context, android.R.color.black)
                    )
                    textViewGraphTaskDate.setTextColor(
                        ContextCompat.getColor(root.context, android.R.color.darker_gray)
                    )
                }

                try {
                    val dateTime = LocalDateTime.parse(
                        graphTask.task.timestamp.toString(),
                        inputFormatter
                    )
                    textViewGraphTaskDate.text = dateTime.format(outputFormatter)
                } catch (e: Exception) {
                    textViewGraphTaskDate.text = graphTask.task.timestamp.toString()
                }

                // Handle selection mode
                if (isSelectionMode) {
                    checkBoxGraphTask.alpha = 0f
                    checkBoxGraphTask.visibility = View.VISIBLE
                    checkBoxGraphTask.animate()
                        .alpha(1f)
                        .setDuration(50)
                        .start()
                } else {
                    checkBoxGraphTask.animate()
                        .alpha(0f)
                        .setDuration(50)
                        .withEndAction {
                            checkBoxGraphTask.visibility = View.GONE
                        }
                        .start()
                }

                checkBoxGraphTask.isChecked = isSelected
                root.isActivated = isSelected
                checkBoxGraphTask.isClickable = false
                checkBoxGraphTask.isFocusable = false
            }
        }
    }
}

private class GraphTaskDiffCallback : DiffUtil.ItemCallback<GraphTaskWithSteps>() {
    override fun areItemsTheSame(oldItem: GraphTaskWithSteps, newItem: GraphTaskWithSteps): Boolean {
        return oldItem.task.id == newItem.task.id
    }

    override fun areContentsTheSame(oldItem: GraphTaskWithSteps, newItem: GraphTaskWithSteps): Boolean {
        return oldItem == newItem
    }
}