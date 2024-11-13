package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphTaskMenuBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        observeGraphTasks()
    }

    private fun setupRecyclerView() {
        adapter = GraphTaskAdapter()
        binding.recyclerViewGraphTasks.apply {
            adapter = this@GraphTaskMenuFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        adapter.setOnItemClickListener { graphTask ->
//            findNavController().navigate(
//                GraphTaskMenuFragmentDirections.actionNavigationGraphTaskMenuToNavigationEditGraphTask(
//                    graphTask.task.id
//                )
//            )
        }

        adapter.setOnSelectionChangedListener { selectedCount ->
            updateFabVisibility(selectedCount)
        }
    }

    private fun updateFabVisibility(selectedCount: Int) {
        binding.apply {
            val isVisible = selectedCount > 0
            fabDelete.visibility = if (isVisible) View.VISIBLE else View.GONE
            fabSelectAll.visibility = if (isVisible) View.VISIBLE else View.GONE

            // Update select/deselect FAB icon and contentDescription
            if (adapter.isAllSelected()) {
                fabSelectAll.setImageResource(R.drawable.baseline_deselect_24)
                fabSelectAll.contentDescription = "Deselect all graph tasks"
            } else {
                fabSelectAll.setImageResource(R.drawable.baseline_select_all_24)
                fabSelectAll.contentDescription = "Select all graph tasks"
            }
        }
    }

    private fun setupClickListeners() {
        binding.addNewGraphTask.setOnClickListener {
            findNavController().navigate(
                GraphTaskMenuFragmentDirections.actionNavigationGraphTaskMenuToNavigationEditGraphTask()
            )
        }

        binding.fabSelectAll.setOnClickListener {
            if (adapter.isAllSelected()) {
                adapter.deselectAllTasks()
            } else {
                adapter.selectAllTasks()
            }
        }

        binding.fabDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedTasks = adapter.getSelectedTasks()
        val taskCount = selectedTasks.size

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Graph Tasks")
            .setMessage("Are you sure you want to delete $taskCount selected task${if (taskCount > 1) "s" else ""}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSelectedTasks()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSelectedTasks() {
        val selectedTasks = adapter.getSelectedTasks()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.deleteGraphTasks(selectedTasks)

                // Show success message
                Snackbar.make(
                    binding.root,
                    "Successfully deleted ${selectedTasks.size} task${if (selectedTasks.size > 1) "s" else ""}",
                    Snackbar.LENGTH_SHORT
                ).show()

                // Exit selection mode and update FAB visibility
                adapter.toggleSelectionMode()
                updateFabVisibility(0)

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

    private fun observeGraphTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allGraphTasks.collectLatest { tasks ->
                // Log the tasks
                Log.d(TAG, "Current graph tasks (${tasks.size} total):")
                tasks.forEachIndexed { index, taskWithSteps ->
                    Log.d(TAG, """
                        Task ${index + 1}:
                        ID: ${taskWithSteps.task.id}
                        Title: ${taskWithSteps.task.title}
                        Description: ${taskWithSteps.task.description}
                        Steps (${taskWithSteps.steps.size}):
                        ${taskWithSteps.steps.joinToString("\n") {
                        "- ${it.description} " +
                                "(Finishing: ${it.isFinishing}, " +
                                "Gratification: ${it.isGratification})"
                    }}
                        ----------------------------------------
                    """.trimIndent())
                }

                // Update the UI
                adapter.submitList(tasks)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}