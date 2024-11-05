package com.sharc.ramdhd.ui.dashboard.routines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.data.model.RoutineWithSteps
import com.sharc.ramdhd.databinding.ItemRoutineBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RoutineAdapter : ListAdapter<RoutineWithSteps, RoutineAdapter.RoutineViewHolder>(RoutineDiffCallback()) {
    private var selectionMode = false
    private val selectedRoutines = mutableSetOf<RoutineWithSteps>()
    private var onItemClickListener: ((RoutineWithSteps) -> Unit)? = null
    private var onItemLongClickListener: ((RoutineWithSteps) -> Unit)? = null
    private var onSelectionChanged: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (RoutineWithSteps) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (RoutineWithSteps) -> Unit) {
        onItemLongClickListener = listener
    }

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChanged = listener
    }

    fun toggleSelectionMode() {
        selectionMode = !selectionMode
        if (!selectionMode) {
            selectedRoutines.clear()
        }
        notifyDataSetChanged()
    }

    fun selectAllRoutines() {
        selectedRoutines.clear()
        currentList.forEach { routine ->
            selectedRoutines.add(routine)
        }
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedRoutines.size)
    }

    fun deselectAllRoutines() {
        selectedRoutines.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }

    fun isAllSelected(): Boolean = selectedRoutines.size == currentList.size

    fun isInSelectionMode() = selectionMode

    fun getSelectedRoutines(): Set<RoutineWithSteps> = selectedRoutines.toSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val binding = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routineWithSteps = getItem(position)
        holder.bind(routineWithSteps, selectionMode, selectedRoutines.contains(routineWithSteps))

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                if (selectedRoutines.contains(routineWithSteps)) {
                    selectedRoutines.remove(routineWithSteps)
                } else {
                    selectedRoutines.add(routineWithSteps)
                }
                notifyItemChanged(position)
                onSelectionChanged?.invoke(selectedRoutines.size)
            } else {
                onItemClickListener?.invoke(routineWithSteps)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                onItemLongClickListener?.invoke(routineWithSteps)
                toggleSelectionMode()
                selectedRoutines.add(routineWithSteps)
                notifyDataSetChanged()
                onSelectionChanged?.invoke(selectedRoutines.size)
            }
            true
        }
    }

    class RoutineViewHolder(private val binding: ItemRoutineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        private val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        fun bind(routineWithSteps: RoutineWithSteps, isSelectionMode: Boolean, isSelected: Boolean) {
            binding.apply {
                textViewRoutineTitle.text = routineWithSteps.routine.title
                try {
                    val dateTime = LocalDateTime.parse(
                        routineWithSteps.routine.timestamp.toString(),
                        inputFormatter
                    )
                    textViewRoutineDate.text = dateTime.format(outputFormatter)
                } catch (e: Exception) {
                    textViewRoutineDate.text = routineWithSteps.routine.timestamp.toString()
                }

                if (isSelectionMode) {
                    checkBoxRoutine.alpha = 0f
                    checkBoxRoutine.visibility = View.VISIBLE
                    checkBoxRoutine.animate()
                        .alpha(1f)
                        .setDuration(50)
                        .start()
                } else {
                    checkBoxRoutine.animate()
                        .alpha(0f)
                        .setDuration(50)
                        .withEndAction {
                            checkBoxRoutine.visibility = View.GONE
                        }
                        .start()
                }

                checkBoxRoutine.isChecked = isSelected
                root.isActivated = isSelected
                checkBoxRoutine.isClickable = false
                checkBoxRoutine.isFocusable = false
            }
        }
    }

    class RoutineDiffCallback : DiffUtil.ItemCallback<RoutineWithSteps>() {
        override fun areItemsTheSame(
            oldItem: RoutineWithSteps,
            newItem: RoutineWithSteps
        ): Boolean {
            return oldItem.routine.id == newItem.routine.id
        }

        override fun areContentsTheSame(
            oldItem: RoutineWithSteps,
            newItem: RoutineWithSteps
        ): Boolean {
            return oldItem == newItem
        }
    }
}