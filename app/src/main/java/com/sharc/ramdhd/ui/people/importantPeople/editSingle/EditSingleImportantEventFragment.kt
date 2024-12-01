package com.sharc.ramdhd.ui.people.importantPeople.editSingle

import android.os.Bundle
import android.util.Log
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
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar
import java.util.Locale

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
            updateDateHeader(year, month, dayOfMonth)
        }

        binding.dateHeaderSection.setOnClickListener {
            showMonthYearPickerDialog()
        }

        // Set initial header
        val calendar = Calendar.getInstance().apply {
            timeInMillis = binding.calendarView.date
        }
        updateDateHeader(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun updateDateHeader(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance().apply {
            set(year, month, dayOfMonth)
        }

        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())

        binding.yearText.text = year.toString()
        binding.fullDateText.text = "$dayOfWeek, $monthName $dayOfMonth"
    }

    private fun showMonthYearPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)

        // Get current date from CalendarView
        val calendar = Calendar.getInstance().apply {
            timeInMillis = binding.calendarView.date
        }

        // Setup year picker
        yearPicker.apply {
            minValue = 1900
            maxValue = LocalDateTime.now().year + 100
            value = calendar.get(Calendar.YEAR)
        }

        // Setup month picker with names
        monthPicker.apply {
            minValue = 0
            maxValue = 11
            displayedValues = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            value = calendar.get(Calendar.MONTH)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Month and Year")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                calendar.set(Calendar.YEAR, yearPicker.value)
                calendar.set(Calendar.MONTH, monthPicker.value)
                binding.calendarView.date = calendar.timeInMillis

                updateDateHeader(
                    yearPicker.value,
                    monthPicker.value,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                viewModel.setSelectedDate(
                    yearPicker.value,
                    monthPicker.value,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Rest of your existing code remains the same
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