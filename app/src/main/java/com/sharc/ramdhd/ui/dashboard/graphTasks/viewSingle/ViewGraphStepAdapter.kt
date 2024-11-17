package com.sharc.ramdhd.ui.dashboard.graphTasks.viewSingle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.model.graphTask.GraphStep

class ViewGraphStepAdapter : ListAdapter<GraphStep, ViewGraphStepAdapter.ViewHolder>(GraphStepDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_graph_step, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = getItem(position)
        holder.bind(step)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val stepNumber: TextView = view.findViewById(R.id.textViewStepNumber)
        private val description: TextView = view.findViewById(R.id.textViewStepDescription)
        private val gratificationIcon: ImageView = view.findViewById(R.id.imageViewGratification)
        private val finishingIcon: ImageView = view.findViewById(R.id.imageViewFinishing)

        fun bind(step: GraphStep) {
            stepNumber.text = (step.orderNumber + 1).toString()
            description.text = step.description

            gratificationIcon.visibility = if (step.isGratification) View.VISIBLE else View.GONE
            finishingIcon.visibility = if (step.isFinishing) View.VISIBLE else View.GONE
        }
    }

    private class GraphStepDiffCallback : DiffUtil.ItemCallback<GraphStep>() {
        override fun areItemsTheSame(oldItem: GraphStep, newItem: GraphStep) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GraphStep, newItem: GraphStep) =
            oldItem == newItem
    }
}