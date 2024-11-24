package com.sharc.ramdhd.ui.people.importantPeople.editSingle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.sharc.ramdhd.databinding.FragmentEditSingleImportantEventBinding

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

        setupCalendarView()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.setSelectedDate(year, month, dayOfMonth)
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val personName = binding.personNameInput.text.toString()
            val eventTitle = binding.eventTitleInput.text.toString()
            val description = binding.descriptionInput.text.toString()

            viewModel.saveEvent(personName, eventTitle, description)
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
        binding.eventTitleInput.text?.clear()
        binding.descriptionInput.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}