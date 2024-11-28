package com.sharc.ramdhd.ui.people.importantPeople

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentImportantPeopleMenuBinding
import com.sharc.ramdhd.data.model.importantPeople.EventType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharc.ramdhd.ui.people.importantPeople.ImportantEventAdapter

class ImportantPeopleMenuFragment : Fragment(R.layout.fragment_important_people_menu) {
    private var _binding: FragmentImportantPeopleMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImportantPeopleMenuViewModel by viewModels()
    private lateinit var eventAdapter: ImportantEventAdapter
    private lateinit var filterAdapter: ArrayAdapter<String>
    private var isAllSelected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportantPeopleMenuBinding.bind(view)

        initializeAdapters()
        setupRecyclerView()
        setupFilterDropdown()
        setupClickListeners()
        setupSelectionModeListeners()
        setupBackPressHandler()
        observeViewModel()
    }

    private fun initializeAdapters() {
        eventAdapter = ImportantEventAdapter()
        filterAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_list_event_options,
            EventFilterType.values().map { it.toString() }.toMutableList()
        )
    }

    private fun setupRecyclerView() {
        binding.eventsRecyclerView.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // For editing existing events
        eventAdapter.setOnItemClickListener { event ->
            if (!eventAdapter.isInSelectionMode()) {
                val action = ImportantPeopleMenuFragmentDirections
                    .actionNavigationImportantPeopleMenuToNavigationEditSingleImportantEvent(
                        eventId = event.id,
                        personName = event.personName,
                        eventType = event.eventType.name,
                        eventName = event.eventName,
                        eventDate = event.eventDate.toString(),
                        description = event.description
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun setupFilterDropdown() {
        val filterOptions = EventFilterType.values().map { it.toString() }
        filterAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_list_event_options,
            filterOptions
        )

        binding.filterAutoComplete.apply {
            threshold = 1000 // Prevent automatic filtering
            setAdapter(filterAdapter)
            setText(EventFilterType.ALL.toString(), false)

            setOnClickListener {
                if (!isPopupShowing) {
                    clearFocus()
                    setAdapter(filterAdapter)
                    showDropDown()
                }
            }

            setOnItemClickListener { _, _, position, _ ->
                val selectedFilter = EventFilterType.values()[position]
                handleFilterSelection(selectedFilter)
            }
        }
    }

    private fun handleFilterSelection(selectedFilter: EventFilterType) {
        when (selectedFilter) {
            EventFilterType.BY_PERSON -> showPersonSelectionDialog()
            EventFilterType.BY_EVENT_TYPE -> showEventTypeSelectionDialog()
            else -> {
                viewModel.setFilter(selectedFilter)
                binding.filterAutoComplete.setText(selectedFilter.toString(), false)
            }
        }
    }

    private fun showPersonSelectionDialog() {
        viewModel.uniquePersonNames.value?.let { names ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Person")
                .setItems(names.toTypedArray()) { _, position ->
                    viewModel.filterByPerson(names[position])
                    binding.filterAutoComplete.setText("By Person: ${names[position]}")
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    resetFilter()
                }
                .show()
        } ?: resetFilter()
    }

    private fun showEventTypeSelectionDialog() {
        val eventTypes = EventType.values()
        val eventTypeNames = eventTypes.map { it.toString().capitalize() }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Event Type")
            .setItems(eventTypeNames) { _, position ->
                viewModel.filterByEventType(eventTypes[position])
                binding.filterAutoComplete.setText("By Type: ${eventTypeNames[position]}")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                resetFilter()
            }
            .show()
    }

    private fun resetFilter() {
        binding.filterAutoComplete.setText(EventFilterType.ALL.toString(), false)
        viewModel.setFilter(EventFilterType.ALL)
    }

    private fun setupClickListeners() {
        // FAB for creating new event
        binding.addNewEventFab.setOnClickListener {
            val action = ImportantPeopleMenuFragmentDirections
                .actionNavigationImportantPeopleMenuToNavigationEditSingleImportantEvent(
                    eventId = -1,
                    personName = null,
                    eventType = null,
                    eventName = null,
                    eventDate = null,
                    description = null
                )
            findNavController().navigate(action)
        }
    }

    private fun setupSelectionModeListeners() {
        // Handle long-click selection mode
        eventAdapter.setOnItemLongClickListener { event ->
            binding.addNewEventFab.visibility = View.GONE
            binding.fabSelectAll.visibility = View.VISIBLE
            binding.fabDelete.visibility = View.VISIBLE
            isAllSelected = false
            binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
        }

        // Handle selection changes
        eventAdapter.setOnSelectionChangedListener { selectedCount ->
            if (selectedCount == 0 && eventAdapter.isInSelectionMode()) {
                exitSelectionMode()
            }
            isAllSelected = eventAdapter.isAllSelected()
            binding.fabSelectAll.setImageResource(
                if (isAllSelected) R.drawable.baseline_deselect_24
                else R.drawable.baseline_select_all_24
            )
        }

        // Handle select all button
        binding.fabSelectAll.setOnClickListener {
            if (isAllSelected) {
                eventAdapter.deselectAllEvents()
                isAllSelected = false
                binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
            } else {
                eventAdapter.selectAllEvents()
                isAllSelected = true
                binding.fabSelectAll.setImageResource(R.drawable.baseline_deselect_24)
            }
        }

        // Handle delete button
        binding.fabDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedEvents = eventAdapter.getSelectedEvents()
        if (selectedEvents.isEmpty()) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Events")
            .setMessage("Are you sure you want to delete ${selectedEvents.size} selected events?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteSelectedEvents()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSelectedEvents() {
        val selectedEvents = eventAdapter.getSelectedEvents().toList()
        viewModel.deleteEvents(selectedEvents)
        exitSelectionMode()
    }

    private fun exitSelectionMode() {
        eventAdapter.toggleSelectionMode()
        binding.addNewEventFab.visibility = View.VISIBLE
        binding.fabSelectAll.visibility = View.GONE
        binding.fabDelete.visibility = View.GONE
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (eventAdapter.isInSelectionMode()) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }

        viewModel.uniquePersonNames.observe(viewLifecycleOwner) { names ->
            // Update person names if needed
        }
    }

    override fun onResume() {
        super.onResume()
        binding.filterAutoComplete.apply {
            setAdapter(null) // Clear the old adapter
            setAdapter(filterAdapter) // Set it again
            setText(EventFilterType.ALL.toString(), false)
        }
        viewModel.setFilter(EventFilterType.ALL)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}