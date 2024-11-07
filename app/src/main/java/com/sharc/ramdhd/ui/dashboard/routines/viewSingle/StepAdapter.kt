package com.sharc.ramdhd.ui.dashboard.routines.viewSingle

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.data.model.Step
import com.sharc.ramdhd.databinding.ItemRoutineStepBinding

class StepAdapter : ListAdapter<Step, StepAdapter.StepViewHolder>(StepDiffCallback()) {
    private var onStepCheckedListener: ((Int, Boolean) -> Unit)? = null
    private var onAllStepsCheckedListener: (() -> Unit)? = null
    private var onNotAllStepsCheckedListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        Log.d("StepAdapter", "onCreateViewHolder called")
        val binding = ItemRoutineStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = getItem(position)
        holder.bind(step, step.isChecked) { isChecked ->
            // Update the model
            getItem(position).isChecked = isChecked

            // Notify listeners
            onStepCheckedListener?.invoke(step.id, isChecked)

            // Check completion state
            checkCompletionState()
        }
    }

    private fun checkCompletionState() {
        val allChecked = currentList.all { it.isChecked }
        Log.d("StepAdapter", "Checking completion state: allChecked=$allChecked")

        if (allChecked && currentList.isNotEmpty()) {
            Log.d("StepAdapter", "All steps are checked")
            onAllStepsCheckedListener?.invoke()
        } else {
            Log.d("StepAdapter", "Not all steps are checked")
            onNotAllStepsCheckedListener?.invoke()
        }
    }

    fun setOnStepCheckedListener(listener: (Int, Boolean) -> Unit) {
        onStepCheckedListener = listener
    }

    fun setOnAllStepsCheckedListener(listener: () -> Unit) {
        onAllStepsCheckedListener = listener
    }

    fun setOnNotAllStepsCheckedListener(listener: () -> Unit) {
        onNotAllStepsCheckedListener = listener
    }

    class StepViewHolder(private val binding: ItemRoutineStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(step: Step, isChecked: Boolean, onCheckChanged: (Boolean) -> Unit) {
            binding.apply {
                textViewStepDescription.text = step.description

                // Remove the listener before setting checked state
                checkBoxStep.setOnCheckedChangeListener(null)

                // Set the checked state
                checkBoxStep.isChecked = isChecked

                // Set the new listener
                checkBoxStep.setOnCheckedChangeListener { _, checked ->
                    onCheckChanged(checked)
                }
            }
        }
    }

    class StepDiffCallback : DiffUtil.ItemCallback<Step>() {
        override fun areItemsTheSame(oldItem: Step, newItem: Step): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Step, newItem: Step): Boolean {
            return oldItem == newItem && oldItem.isChecked == newItem.isChecked
        }
    }

    override fun submitList(list: List<Step>?) {
        super.submitList(list?.map { it.copy() })

        // Check initial completion state after submitting the list
        if (list != null) {
            checkCompletionState()
        }
    }
}