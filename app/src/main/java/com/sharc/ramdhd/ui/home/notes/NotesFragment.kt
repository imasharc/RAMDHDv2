package com.sharc.ramdhd.ui.home.notes

import NotesViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback  // Add this import
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentNotesBinding
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter
    private var isAllSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeNotes()
        setupListeners()
        setupBackPressHandler()  // Add this line
    }

    private fun setupListeners() {
        binding.myImageView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notes_to_navigation_edit_note)
        }

        binding.fabSelectAll.setOnClickListener {
            if (isAllSelected) {
                noteAdapter.deselectAllNotes()
                isAllSelected = false
                binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
            } else {
                noteAdapter.selectAllNotes()
                isAllSelected = true
                binding.fabSelectAll.setImageResource(R.drawable.baseline_deselect_24)
            }
        }

        noteAdapter.setOnItemLongClickListener { note ->
            // Enter selection mode
            binding.myImageView.visibility = View.GONE
            binding.fabSelectAll.visibility = View.VISIBLE
            binding.fabDelete.visibility = View.VISIBLE  // Show delete FAB
            isAllSelected = false
            binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
        }

        binding.fabDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        noteAdapter.setOnSelectionChangedListener { selectedCount ->
            if (selectedCount == 0 && noteAdapter.isInSelectionMode()) {
                exitSelectionMode()
            }
            // Update select all button icon based on selection state
            isAllSelected = noteAdapter.isAllSelected()
            binding.fabSelectAll.setImageResource(
                if (isAllSelected) R.drawable.baseline_deselect_24
                else R.drawable.baseline_select_all_24
            )
        }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedNotes = noteAdapter.getSelectedNotes()
        if (selectedNotes.isEmpty()) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Notes")
            .setMessage("Are you sure you want to delete ${selectedNotes.size} selected notes?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteSelectedNotes()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSelectedNotes() {
        val selectedNotes = noteAdapter.getSelectedNotes().toList()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteNotes(selectedNotes)
            exitSelectionMode()
        }
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (noteAdapter.isInSelectionMode()) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun exitSelectionMode() {
        noteAdapter.toggleSelectionMode()
        binding.myImageView.visibility = View.VISIBLE
        binding.fabSelectAll.visibility = View.GONE
        binding.fabDelete.visibility = View.GONE  // Hide delete FAB
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter()
        binding.recyclerViewNotes.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun observeNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allNotes.collect { notes ->
                val sortedNotes = notes.sortedBy { it.timestamp }
                noteAdapter.submitList(sortedNotes)
                // Log notes for debugging
                sortedNotes.forEach { note ->
                    Log.d("NotesFragment", "Note ${note.id}: ${note.title} - ${note.description} at ${note.timestamp}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}