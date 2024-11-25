package com.sharc.ramdhd.ui.people.importantPeople

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportantPeopleMenuBinding.bind(view)

        initializeAdapters()
        setupRecyclerView()
        setupFilterDropdown()
        setupClickListeners()
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

        eventAdapter.setOnItemClickListener { event ->
            val action = ImportantPeopleMenuFragmentDirections
                .actionNavigationImportantPeopleMenuToNavigationEditSingleImportantEvent()
            findNavController().navigate(action)
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
        // Simply apply the filter directly
        viewModel.setFilter(selectedFilter)
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

    override fun onResume() {
        super.onResume()
        binding.filterAutoComplete.apply {
            setAdapter(null) // Clear the old adapter
            setAdapter(filterAdapter) // Set it again
            setText(EventFilterType.ALL.toString(), false)
        }
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

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }

        viewModel.uniquePersonNames.observe(viewLifecycleOwner) { names ->
            // Update person names if needed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}