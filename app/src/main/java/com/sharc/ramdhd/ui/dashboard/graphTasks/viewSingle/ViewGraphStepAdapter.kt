package com.sharc.ramdhd.ui.dashboard.graphTasks.viewSingle
import android.util.Log
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
        val isFirstItem = position == 0
        val isLastItem = position == itemCount - 1
        holder.bind(step, onStepIconClicked, onStepCompletionChanged, isFirstItem, isLastItem)
    }

    class ViewHolder(private val binding: ItemViewGraphStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            step: GraphStep,
            onStepIconClicked: (GraphStep) -> Unit,
            onStepCompletionChanged: (GraphStep, Boolean) -> Unit,
            isFirstItem: Boolean,
            isLastItem: Boolean
        ) {
            binding.apply {
                // Handle vertical line
                verticalLine.visibility = when {
                    isFirstItem && isLastItem -> View.GONE  // Single item - no line
                    isFirstItem -> View.VISIBLE             // Only bottom half visible
                    isLastItem -> View.VISIBLE             // Only top half visible
                    else -> View.VISIBLE                   // Both halves visible
                }

                // Adjust line height and position for first/last items
                if (isFirstItem) {
                    verticalLine.translationY = 50f  // Move line down to show only bottom half
                } else if (isLastItem) {
                    verticalLine.translationY = -100f  // Move line up to show only top half
                } else {
                    verticalLine.translationY = 0f   // Show full line
                }

                verticalLine.setBackgroundColor(
                    if (step.isCompleted)
                        ContextCompat.getColor(root.context, android.R.color.holo_green_light)
                    else
                        ContextCompat.getColor(root.context, R.color.purple_500)
                )

                // Handle icon display
                when {
                    step.icon != null -> {
                        imageViewStepIcon.visibility = View.GONE
                        textViewStepIcon.visibility = View.VISIBLE
                        textViewStepIcon.text = step.icon
                    }
                    else -> {
                        imageViewStepIcon.visibility = View.VISIBLE
                        textViewStepIcon.visibility = View.GONE
                        imageViewStepIcon.setImageResource(R.drawable.media_image)
                    }
                }

                textViewStepDescription.text = step.description

                // Remove previous listeners
                checkboxComplete.setOnCheckedChangeListener(null)
                mainContent.setOnClickListener(null)

                // Set up click listener for both icon views
                val iconClickListener = View.OnClickListener { onStepIconClicked(step) }
                imageViewStepIcon.setOnClickListener(iconClickListener)
                textViewStepIcon.setOnClickListener(iconClickListener)

                // Set the checked state
                checkboxComplete.isChecked = step.isCompleted
                checkboxComplete.isClickable = false
                checkboxComplete.isFocusable = false

                // Set background color based on completion
                mainContent.setBackgroundColor(
                    if (step.isCompleted)
                        ContextCompat.getColor(root.context, android.R.color.holo_green_light)
                    else
                        ContextCompat.getColor(root.context, android.R.color.white)
                )

                // Set click listener for the entire item
                mainContent.setOnClickListener {
                    onStepCompletionChanged(step, !step.isCompleted)
                }

                // Set icon visibility
                imageViewGratification.visibility = if (step.isGratification) View.VISIBLE else View.GONE
                imageViewFinishing.visibility = if (step.isFinishing) View.VISIBLE else View.GONE
//
//                // Ensure proper z-ordering
//                mainContent.elevation = 1f
//                verticalLine.elevation = 0f
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
        Log.d("ViewGraphStepAdapter", "Submitting list of size: ${list?.size}")
        list?.forEachIndexed { index, step ->
            Log.d("ViewGraphStepAdapter", "Step $index: ${step.description}")
        }
        super.submitList(list?.map { it.copy() })
    }
}