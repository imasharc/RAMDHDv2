package com.sharc.ramdhd.ui.dashboard.graphTasks.viewSingle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.model.graphTask.GraphStep
import com.sharc.ramdhd.databinding.ItemViewGraphStepBinding

class ViewGraphStepAdapter(
    private val onStepIconClicked: (GraphStep) -> Unit,
    private val onStepCompletionChanged: (GraphStep, Boolean) -> Unit
) : ListAdapter<GraphStep, ViewGraphStepAdapter.ViewHolder>(GraphStepDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemViewGraphStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = getItem(position)
        holder.bind(step, onStepIconClicked, onStepCompletionChanged)
    }

    class ViewHolder(private val binding: ItemViewGraphStepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            step: GraphStep,
            onStepIconClicked: (GraphStep) -> Unit,
            onStepCompletionChanged: (GraphStep, Boolean) -> Unit
        ) {
            binding.apply {
                // Handle icon display
                when {
                    step.icon != null -> {
                        // Show custom icon (emoji or text)
                        imageViewStepIcon.visibility = View.GONE
                        textViewStepIcon.visibility = View.VISIBLE
                        textViewStepIcon.text = step.icon
                    }
                    else -> {
                        // Show default media SVG
                        imageViewStepIcon.visibility = View.VISIBLE
                        textViewStepIcon.visibility = View.GONE
                        imageViewStepIcon.setImageResource(R.drawable.media_image)
                    }
                }

                textViewStepDescription.text = step.description

                // Remove previous listeners
                checkboxComplete.setOnCheckedChangeListener(null)
                root.setOnClickListener(null)

                // Set up click listener for both icon views
                val iconClickListener = View.OnClickListener { onStepIconClicked(step) }
                imageViewStepIcon.setOnClickListener(iconClickListener)
                textViewStepIcon.setOnClickListener(iconClickListener)

                // Set the checked state
                checkboxComplete.isChecked = step.isCompleted
                checkboxComplete.isClickable = false
                checkboxComplete.isFocusable = false

                // Set background color based on completion
                root.setBackgroundColor(
                    if (step.isCompleted)
                        ContextCompat.getColor(root.context, android.R.color.holo_green_light)
                    else
                        ContextCompat.getColor(root.context, android.R.color.white)
                )

                // Set click listener for the entire item
                root.setOnClickListener {
                    onStepCompletionChanged(step, !step.isCompleted)
                }

                // Set icon visibility
                imageViewGratification.visibility = if (step.isGratification) View.VISIBLE else View.GONE
                imageViewFinishing.visibility = if (step.isFinishing) View.VISIBLE else View.GONE
            }
        }
    }

    private class GraphStepDiffCallback : DiffUtil.ItemCallback<GraphStep>() {
        override fun areItemsTheSame(oldItem: GraphStep, newItem: GraphStep): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: GraphStep, newItem: GraphStep): Boolean {
            return oldItem == newItem
        }
    }

    override fun submitList(list: List<GraphStep>?) {
        super.submitList(list?.map { it.copy() })
    }
}