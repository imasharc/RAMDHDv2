package com.sharc.ramdhd.ui.dashboard.routines

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentRoutineMenuBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoutineMenuFragment : Fragment(R.layout.fragment_routine_menu) {
    private var _binding: FragmentRoutineMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineMenuViewModel by viewModels()
    private lateinit var routineAdapter: RoutineAdapter
    private var isAllSelected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoutineMenuBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        setupBackPressHandler()
        observeRoutines()
    }

    private fun setupRecyclerView() {
        routineAdapter = RoutineAdapter()
        binding.recyclerViewRoutines.apply {
            adapter = routineAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.myImageView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_routine_menu_to_navigation_edit_routine)
        }

        binding.fabSelectAll.setOnClickListener {
            if (isAllSelected) {
                routineAdapter.deselectAllRoutines()
                isAllSelected = false
                binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
            } else {
                routineAdapter.selectAllRoutines()
                isAllSelected = true
                binding.fabSelectAll.setImageResource(R.drawable.baseline_deselect_24)
            }
        }

        routineAdapter.setOnItemLongClickListener { routine ->
            // Enter selection mode
            binding.myImageView.visibility = View.GONE
            binding.fabSelectAll.visibility = View.VISIBLE
            binding.fabDelete.visibility = View.VISIBLE
            isAllSelected = false
            binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
        }

        binding.fabDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        routineAdapter.setOnSelectionChangedListener { selectedCount ->
            if (selectedCount == 0 && routineAdapter.isInSelectionMode()) {
                exitSelectionMode()
            }
            // Update select all button icon based on selection state
            isAllSelected = routineAdapter.isAllSelected()
            binding.fabSelectAll.setImageResource(
                if (isAllSelected) R.drawable.baseline_deselect_24
                else R.drawable.baseline_select_all_24
            )
        }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedRoutines = routineAdapter.getSelectedRoutines()
        if (selectedRoutines.isEmpty()) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Routines")
            .setMessage("Are you sure you want to delete ${selectedRoutines.size} selected routines?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteSelectedRoutines()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSelectedRoutines() {
        val selectedRoutines = routineAdapter.getSelectedRoutines().toList()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteRoutines(selectedRoutines.map { it.routine })
            exitSelectionMode()
        }
    }

    private fun exitSelectionMode() {
        routineAdapter.toggleSelectionMode()
        binding.myImageView.visibility = View.VISIBLE
        binding.fabSelectAll.visibility = View.GONE
        binding.fabDelete.visibility = View.GONE
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (routineAdapter.isInSelectionMode()) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun observeRoutines() {
        lifecycleScope.launch {
            viewModel.routines.collectLatest { routines ->
                routineAdapter.submitList(routines.sortedByDescending { it.routine.timestamp })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}