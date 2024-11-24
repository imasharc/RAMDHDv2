package com.sharc.ramdhd.ui.people.importantPeople.editSingle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.model.importantPeople.EventType
import com.sharc.ramdhd.data.model.importantPeople.RecurrenceType
import com.sharc.ramdhd.databinding.FragmentEditSingleImportantEventBinding
import java.util.Locale

class EditSingleImportantEventFragment : Fragment() {
    private var _binding: FragmentEditSingleImportantEventBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditSingleImportantEventViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSingleImportantEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupCalendarView()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.setSelectedDate(year, month, dayOfMonth)
        }
    }

    private fun setupDropdowns() {
        // Setup Event Type dropdown
        val eventTypes = EventType.values().map { it.name.lowercase(Locale.ROOT).capitalize(Locale.ROOT) }
        val eventTypeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_list_event_options,
            eventTypes
        )
        binding.eventTypeDropdown.setAdapter(eventTypeAdapter)

        // Setup Recurrence Type dropdown
        val recurrenceTypes = RecurrenceType.values().map { it.name.lowercase(Locale.ROOT).capitalize() }
        val recurrenceTypeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_list_event_options,
            recurrenceTypes
        )
        binding.recurrenceTypeDropdown.setAdapter(recurrenceTypeAdapter)

        // Handle event type selection
        binding.eventTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedEventType = EventType.values()[position]
            // Set default recurrence based on event type
            val defaultRecurrence = when (selectedEventType) {
                EventType.ANNIVERSARY -> RecurrenceType.YEARLY
                EventType.MEETUP -> RecurrenceType.NONE
                EventType.CONTACTING -> RecurrenceType.WEEKLY
            }
            binding.recurrenceTypeDropdown.setText(defaultRecurrence.name.lowercase(Locale.ROOT).capitalize(), false)

            // Enable/disable recurrence selection based on event type
            binding.recurrenceTypeDropdown.isEnabled = selectedEventType == EventType.CONTACTING
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val personName = binding.personNameInput.text.toString()
            val eventType = EventType.valueOf(binding.eventTypeDropdown.text.toString().uppercase())
            val eventName = binding.eventNameInput.text.toString()
            val recurrenceType = RecurrenceType.valueOf(binding.recurrenceTypeDropdown.text.toString().uppercase())
            val description = binding.descriptionInput.text.toString()

            viewModel.saveEvent(personName, eventType, eventName, recurrenceType, description)
        }
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                showSuccessMessage()
                clearInputFields()
                requireActivity().onBackPressed()
            } else {
                showErrorMessage()
            }
        }
    }

    private fun showSuccessMessage() {
        Snackbar.make(
            binding.root,
            "Event saved successfully",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showErrorMessage() {
        Snackbar.make(
            binding.root,
            "Please fill all required fields",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun clearInputFields() {
        binding.personNameInput.text?.clear()
        binding.eventNameInput.text?.clear()
        binding.descriptionInput.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}