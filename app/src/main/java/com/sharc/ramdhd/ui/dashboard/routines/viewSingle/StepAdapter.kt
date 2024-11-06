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
        Log.d("StepAdapter", "onBindViewHolder called for position: $position")
        holder.bind(getItem(position))
    }

    class StepViewHolder(private val binding: ItemRoutineStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(step: Step) {
            binding.apply {
                textViewStepDescription.text = step.description
                // You might want to handle checkbox state here
                checkBoxStep.isChecked = false
            }
        }
    }

    class StepDiffCallback : DiffUtil.ItemCallback<Step>() {
        override fun areItemsTheSame(oldItem: Step, newItem: Step): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Step, newItem: Step): Boolean {
            return oldItem == newItem
        }
    }

//    override fun submitList(list: List<Step>?) {
//        Log.d("StepAdapter", "submitList called with size: ${list?.size}")
//        super.submitList(list)
//    }
}