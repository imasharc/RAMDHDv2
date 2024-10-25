package com.sharc.ramdhd.ui.home.notes.editSingle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
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

        // Set existing note data if editing
        if (args.noteId != -1) {
            viewModel.setNoteId(args.noteId)
            binding.editTextTitle.setText(args.noteTitle)
            binding.editTextDescription.setText(args.noteDescription)
        }

        binding.buttonSave.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()

            if (title.isBlank()) {
                binding.editTextTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }

            viewModel.saveNote(title, description)
            Toast.makeText(
                context,
                if (args.noteId == -1) "Note created successfully" else "Note updated successfully",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}