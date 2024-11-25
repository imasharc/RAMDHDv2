package com.sharc.ramdhd.ui.people.importantPeople.editSingle

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.model.importantPeople.EventType
import com.sharc.ramdhd.data.model.importantPeople.RecurrenceType
import com.sharc.ramdhd.databinding.FragmentEditSingleImportantEventBinding
import java.time.LocalDateTime
import java.time.ZoneId

class EditSingleImportantEventFragment : Fragment(R.layout.fragment_edit_single_important_event) {
    private var _binding: FragmentEditSingleImportantEventBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditSingleImportantEventViewModel by viewModels()
    private val args: EditSingleImportantEventFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditSingleImportantEventBinding.bind(view)

        setupCalendarView()
        setupInitialData()
        setupDropdowns()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.setSelectedDate(year, month, dayOfMonth)
        }

        // If editing existing event, set the calendar date
        args.eventDate?.let { dateString ->
            try {
                val date = LocalDateTime.parse(dateString)
                binding.calendarView.date = date
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                viewModel.setSelectedDate(date.year, date.monthValue - 1, date.dayOfMonth)
            } catch (e: Exception) {
                // If parsing fails, use current date
                val now = LocalDateTime.now()
                viewModel.setSelectedDate(now.year, now.monthValue - 1, now.dayOfMonth)
            }
        }
    }

    private fun setupInitialData() {
        if (args.eventId != -1) {
            viewModel.loadEvent(args.eventId)

            binding.apply {
                args.personName?.let { personNameInput.setText(it) }
                args.eventName?.let { eventNameInput.setText(it) }
                args.description?.let { descriptionInput.setText(it) }
                args.eventType?.let { eventTypeDropdown.setText(it) }
            }
        } else {
            // For new events, initialize with current date
            val now = LocalDateTime.now()
            viewModel.setSelectedDate(now.year, now.monthValue - 1, now.dayOfMonth)
        }
    }

    private fun setupDropdowns() {
        // Setup Event Type dropdown
        val eventTypes = EventType.values().map { it.toString().lowercase().capitalize() }
        val eventTypeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_list_event_options,
            eventTypes
        )
        binding.eventTypeDropdown.setAdapter(eventTypeAdapter)

        // Setup Recurrence Type dropdown
        val recurrenceTypes = RecurrenceType.values().map { it.toString().lowercase().capitalize() }
        val recurrenceTypeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_list_event_options,
            recurrenceTypes
        )
        binding.recurrenceTypeDropdown.setAdapter(recurrenceTypeAdapter)

        // Set default recurrence based on event type
        binding.eventTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedEventType = EventType.values()[position]
            val defaultRecurrence = when (selectedEventType) {
                EventType.ANNIVERSARY -> RecurrenceType.YEARLY
                EventType.MEETUP -> RecurrenceType.NONE
                EventType.CONTACTING -> RecurrenceType.WEEKLY
            }
            binding.recurrenceTypeDropdown.setText(
                defaultRecurrence.toString().lowercase().capitalize(),
                false
            )
            binding.recurrenceTypeDropdown.isEnabled = selectedEventType == EventType.CONTACTING
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val personName = binding.personNameInput.text.toString()
            val eventType = try {
                EventType.valueOf(binding.eventTypeDropdown.text.toString().uppercase())
            } catch (e: IllegalArgumentException) {
                EventType.MEETUP // Default value
            }
            val eventName = binding.eventNameInput.text.toString()
            val recurrenceType = try {
                RecurrenceType.valueOf(binding.recurrenceTypeDropdown.text.toString().uppercase())
            } catch (e: IllegalArgumentException) {
                RecurrenceType.NONE // Default value
            }
            val description = binding.descriptionInput.text.toString()

            viewModel.saveEvent(
                eventId = args.eventId,
                personName = personName,
                eventType = eventType,
                eventName = eventName,
                recurrenceType = recurrenceType,
                description = description
            )
        }
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigateUp()
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event?.let {
                binding.apply {
                    personNameInput.setText(it.personName)
                    eventNameInput.setText(it.eventName)
                    descriptionInput.setText(it.description)
                    eventTypeDropdown.setText(it.eventType.toString().lowercase().capitalize(), false)
                    recurrenceTypeDropdown.setText(it.recurrenceType.toString().lowercase().capitalize(), false)

                    // Update calendar with event date
                    calendarView.date = it.eventDate
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    viewModel.setSelectedDate(
                        it.eventDate.year,
                        it.eventDate.monthValue - 1,
                        it.eventDate.dayOfMonth
                    )
                }
            }
        }
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}