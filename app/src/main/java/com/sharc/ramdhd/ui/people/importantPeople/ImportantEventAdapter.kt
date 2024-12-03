package com.sharc.ramdhd.ui.people.importantPeople

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.databinding.ItemEventHeaderBinding
import com.sharc.ramdhd.databinding.ItemImportantEventBinding
import java.time.format.DateTimeFormatter
import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent
import com.sharc.ramdhd.ui.people.importantPeople.EventListItem

class ImportantEventAdapter : ListAdapter<EventListItem, RecyclerView.ViewHolder>(EventDiffCallback()) {
    private var selectionMode = false
    private val selectedEvents = mutableSetOf<ImportantEvent>()
    private var onItemClickListener: ((ImportantEvent) -> Unit)? = null
    private var onItemLongClickListener: ((ImportantEvent) -> Unit)? = null
    private var onSelectionChanged: ((Int) -> Unit)? = null
    private var onSelectionStarted: (() -> Unit)? = null

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EVENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EventListItem.HeaderItem -> TYPE_HEADER
            is EventListItem.EventItem -> TYPE_EVENT
        }
    }

    fun setOnItemClickListener(listener: (ImportantEvent) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (ImportantEvent) -> Unit) {
        onItemLongClickListener = listener
    }

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChanged = listener
    }

    fun setOnSelectionStartedListener(listener: () -> Unit) {
        onSelectionStarted = listener
    }

    fun toggleSelectionMode() {
        selectionMode = !selectionMode
        if (!selectionMode) {
            selectedEvents.clear()
            onSelectionChanged?.invoke(0)
        }
        notifyDataSetChanged()
    }

    fun selectAllEvents() {
        selectedEvents.clear()
        currentList.forEach { item ->
            if (item is EventListItem.EventItem) {
                selectedEvents.add(item.event)
            }
        }
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedEvents.size)
    }

    fun deselectAllEvents() {
        selectedEvents.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }

    fun isAllSelected(): Boolean {
        val totalEvents = currentList.count { it is EventListItem.EventItem }
        return selectedEvents.size == totalEvents && totalEvents > 0
    }

    fun isInSelectionMode() = selectionMode

    fun getSelectedEvents(): Set<ImportantEvent> = selectedEvents.toSet()

    fun exitSelectionMode() {
        if (selectionMode) {
            selectionMode = false
            selectedEvents.clear()
            notifyDataSetChanged()
            onSelectionChanged?.invoke(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemEventHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            TYPE_EVENT -> {
                val binding = ItemImportantEventBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                EventViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EventListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item.title)
            is EventListItem.EventItem -> {
                (holder as EventViewHolder).bind(
                    item.event,
                    selectionMode,
                    selectedEvents.contains(item.event)
                )

                holder.itemView.setOnClickListener {
                    if (selectionMode) {
                        toggleEventSelection(item.event, position)
                    } else {
                        onItemClickListener?.invoke(item.event)
                    }
                }

                holder.itemView.setOnLongClickListener {
                    if (!selectionMode) {
                        startSelectionMode(item.event, position)
                    }
                    true
                }
            }
        }
    }

    private fun toggleEventSelection(event: ImportantEvent, position: Int) {
        if (selectedEvents.contains(event)) {
            selectedEvents.remove(event)
        } else {
            selectedEvents.add(event)
        }
        notifyItemChanged(position)
        onSelectionChanged?.invoke(selectedEvents.size)
    }

    private fun startSelectionMode(event: ImportantEvent, position: Int) {
        selectionMode = true
        selectedEvents.clear()
        selectedEvents.add(event)
        notifyDataSetChanged()
        onSelectionStarted?.invoke()
        onSelectionChanged?.invoke(1)
    }

    class HeaderViewHolder(private val binding: ItemEventHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.headerTitle.text = title
        }
    }

    class EventViewHolder(private val binding: ItemImportantEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        fun bind(event: ImportantEvent, isSelectionMode: Boolean, isSelected: Boolean) {
            binding.apply {
                textViewPersonName.text = event.personName
                textViewEventType.text = event.eventType.toString().lowercase().capitalize()
                textViewEventDate.text = event.eventDate.format(dateFormatter)
                textViewEventName.text = event.eventName

                if (isSelectionMode) {
                    checkBoxEvent.alpha = 0f
                    checkBoxEvent.visibility = View.VISIBLE
                    checkBoxEvent.animate()
                        .alpha(1f)
                        .setDuration(50)
                        .start()
                } else {
                    checkBoxEvent.animate()
                        .alpha(0f)
                        .setDuration(50)
                        .withEndAction {
                            checkBoxEvent.visibility = View.GONE
                        }
                        .start()
                }

                checkBoxEvent.isChecked = isSelected
                root.isActivated = isSelected
                checkBoxEvent.isClickable = false
                checkBoxEvent.isFocusable = false
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<EventListItem>() {
        override fun areItemsTheSame(oldItem: EventListItem, newItem: EventListItem): Boolean {
            return when {
                oldItem is EventListItem.HeaderItem && newItem is EventListItem.HeaderItem ->
                    oldItem.title == newItem.title
                oldItem is EventListItem.EventItem && newItem is EventListItem.EventItem ->
                    oldItem.event.id == newItem.event.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: EventListItem, newItem: EventListItem): Boolean {
            return oldItem == newItem
        }
    }
}