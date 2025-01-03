package com.sharc.ramdhd.ui.home.notes.editSingle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sharc.ramdhd.databinding.FragmentEditNoteBinding

class EditNoteFragment : Fragment() {
    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditNoteViewModel
    private val args: EditNoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EditNoteViewModel::class.java]
        setupInitialData()
        setupSaveButton()
        handleInitialFocus()
    }

    private fun setupInitialData() {
        if (args.noteId != -1) {
            viewModel.setNoteId(args.noteId)
            binding.editTextTitle.setText(args.noteTitle)
            binding.editTextDescription.setText(args.noteDescription)
        }
    }

    private fun checkBatteryOptimization() {
        val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)
        ) {
            showBatteryOptimizationDialog()
        } else {
            saveNote()
        }
    }

    private fun showBatteryOptimizationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Battery Settings")
            .setMessage("To ensure notes remain visible, please disable battery optimization for this app in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openBatterySettings()
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
                saveNote()
            }
            .show()
    }

    private fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Please disable battery optimization for this app in system settings",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            if (title.isBlank()) {
                binding.editTextTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }

            checkBatteryOptimization()
        }
    }

    private fun saveNote() {
        val title = binding.editTextTitle.text.toString()
        val description = binding.editTextDescription.text.toString()
        viewModel.saveNote(title, description)
        Toast.makeText(
            context,
            if (args.noteId == -1) "Note created successfully" else "Note updated successfully",
            Toast.LENGTH_SHORT
        ).show()
        findNavController().navigateUp()
    }

    private fun handleInitialFocus() {
        if (args.noteId == -1) {
            binding.editTextTitle.post {
                binding.editTextTitle.requestFocus()
                showKeyboardFor(binding.editTextTitle)
            }
        }
    }

    private fun showKeyboardFor(view: View) {
        view.post {
            view.isFocusableInTouchMode = true
            view.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}