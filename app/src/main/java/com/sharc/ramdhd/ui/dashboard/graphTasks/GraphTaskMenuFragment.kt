package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentGraphTaskMenuBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GraphTaskMenuFragment : Fragment(R.layout.fragment_graph_task_menu) {
    companion object {
        private const val TAG = "GraphTaskMenuFragment"
    }

    private var _binding: FragmentGraphTaskMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GraphTaskMenuViewModel by viewModels()
    private lateinit var adapter: GraphTaskAdapter
    private var isAllSelected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphTaskMenuBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        observeGraphTasks()
        setupBackPressHandler()
    }

    private fun setupRecyclerView() {
        adapter = GraphTaskAdapter()
        binding.recyclerViewGraphTasks.apply {
            adapter = this@GraphTaskMenuFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
        }

        adapter.setOnItemClickListener { graphTask ->
            if (!adapter.isInSelectionMode()) {
                Log.d(TAG, "Navigating to task with ID: ${graphTask.task.id}")
                findNavController().navigate(
                    GraphTaskMenuFragmentDirections
                        .actionNavigationGraphTaskMenuToNavigationViewSingleGraphTask(
                            taskId = graphTask.task.id
                        )
                )
            }
        }

        adapter.setOnItemLongClickListener { _ ->
            // Enter selection mode
            binding.addNewGraphTask.visibility = View.GONE
            binding.fabSelectAll.visibility = View.VISIBLE
            binding.fabDelete.visibility = View.VISIBLE
            isAllSelected = false
            binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
        }

        adapter.setOnSelectionChangedListener { selectedCount ->
            if (selectedCount == 0 && adapter.isInSelectionMode()) {
                exitSelectionMode()
            }
            isAllSelected = adapter.isAllSelected()
            binding.fabSelectAll.setImageResource(
                if (isAllSelected) R.drawable.baseline_deselect_24
                else R.drawable.baseline_select_all_24
            )
        }
    }

    private fun setupClickListeners() {
        binding.addNewGraphTask.setOnClickListener {
            findNavController().navigate(
                GraphTaskMenuFragmentDirections.actionNavigationGraphTaskMenuToNavigationEditGraphTask()
            )
        }

        binding.fabSelectAll.setOnClickListener {
            if (isAllSelected) {
                adapter.deselectAllTasks()
                isAllSelected = false
                binding.fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
            } else {
                adapter.selectAllTasks()
                isAllSelected = true
                binding.fabSelectAll.setImageResource(R.drawable.baseline_deselect_24)
            }
        }

        binding.fabDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedTasks = adapter.getSelectedTasks()
        if (selectedTasks.isEmpty()) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Graph Tasks")
            .setMessage("Are you sure you want to delete ${selectedTasks.size} selected task${if (selectedTasks.size > 1) "s" else ""}?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteSelectedTasks()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteSelectedTasks() {
        val selectedTasks = adapter.getSelectedTasks().toList()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.deleteGraphTasks(selectedTasks)
                exitSelectionMode()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting tasks", e)
                Snackbar.make(
                    binding.root,
                    "Error deleting tasks",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isInSelectionMode()) {
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
        adapter.toggleSelectionMode()
        binding.addNewGraphTask.visibility = View.VISIBLE
        binding.fabSelectAll.visibility = View.GONE
        binding.fabDelete.visibility = View.GONE
    }

    private fun observeGraphTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allGraphTasks.collectLatest { tasks ->
                // Sort tasks by timestamp if needed
                val sortedTasks = tasks.sortedByDescending { it.task.timestamp }
                adapter.submitList(sortedTasks)

                // Log for debugging
                Log.d(TAG, "Current graph tasks (${tasks.size} total):")
                sortedTasks.forEachIndexed { index, taskWithSteps ->
                    Log.d(TAG, """
                        Task ${index + 1}:
                        ID: ${taskWithSteps.task.id}
                        Title: ${taskWithSteps.task.title}
                        Steps: ${taskWithSteps.steps.size}
                        ----------------------------------------
                    """.trimIndent())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}