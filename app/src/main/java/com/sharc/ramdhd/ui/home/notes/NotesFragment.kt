package com.sharc.ramdhd.ui.home.notes

import NotesViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentNotesBinding
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter

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

        binding.myImageView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notes_to_navigation_edit_note)
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter()
        binding.recyclerViewNotes.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)  // Optimize performance if items have fixed size
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