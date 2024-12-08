package com.sharc.ramdhd.ui.dashboard.graphTasks.viewSingle

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.model.graphTask.GraphStep
import com.sharc.ramdhd.data.repository.GraphTaskRepository
import com.sharc.ramdhd.databinding.FragmentViewSingleGraphTaskBinding
import com.sharc.ramdhd.ui.shared.IconSelectorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewSingleGraphTaskFragment : Fragment(R.layout.fragment_view_single_graph_task) {
    private var _binding: FragmentViewSingleGraphTaskBinding? = null
    private val binding get() = _binding!!
    private val args: ViewSingleGraphTaskFragmentArgs by navArgs()
    private lateinit var stepAdapter: ViewGraphStepAdapter
    private var completionDialogShowing = false
    private var isResetting = false

    private val repository by lazy {
        val db = AppDatabase.getDatabase(requireContext())
        GraphTaskRepository(db.graphTaskDao())
    }

    private val viewModel: ViewSingleGraphTaskViewModel by viewModels {
        ViewSingleGraphTaskViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentViewSingleGraphTaskBinding.bind(view)
        setupViews()
        loadGraphTaskData()
        observeGraphTaskData()

        // Check completion state on create
        viewModel.graphTask.value?.let { task ->
            val allCompleted = task.steps.all { it.isCompleted }
            if (allCompleted && !completionDialogShowing && !isResetting) {
                showCompletionDialog()
            }
        }
    }

    private fun setupViews() {
        stepAdapter = ViewGraphStepAdapter(
            onStepIconClicked = { step ->
                showIconSelector(step)
            },
            onStepCompletionChanged = ::handleStepStateChange  // Use method reference
        )

        binding.recyclerViewSteps.apply {
            adapter = stepAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            // Add this to verify layout parameters
            Log.d(TAG, "RecyclerView height mode: ${layoutParams.height}")
        }

        binding.fabEdit.setOnClickListener {
            navigateToEdit()
        }
    }

    private fun handleStepStateChange(step: GraphStep, isCompleted: Boolean) {
        if (!completionDialogShowing && !isResetting) {
            viewModel.handleStepStateChange(
                stepId = step.id,
                isCompleted = isCompleted,
                taskId = args.taskId
            ) { allCompleted ->
                if (allCompleted) {
                    showCompletionDialog()
                }
            }
        }
    }

    private fun showIconSelector(step: GraphStep) {
        IconSelectorDialog().apply {
            setOnIconSelectedListener { iconOption ->
                viewModel.updateStepIcon(step.id, iconOption.icon)
            }
        }.show(childFragmentManager, "icon_selector")
    }

    private fun showCompletionDialog() {
        if (!isAdded || completionDialogShowing || isResetting) return

        completionDialogShowing = true
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Congratulations!")
            .setMessage("Congratulations on completing this task")
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
                completeTask()
            }
            .setNeutralButton("Reset") { dialog, _ ->
                dialog.dismiss()
                resetTask()
            }
            .setOnDismissListener {
                completionDialogShowing = false
            }
            .setCancelable(false)
            .show()
    }

    private fun completeTask() {
        if (!isAdded) return
        findNavController().navigateUp()
    }

    private fun resetTask() {
        if (!isAdded) return

        isResetting = true
        lifecycleScope.launch {
            try {
                viewModel.resetTask(args.taskId)
                withContext(Dispatchers.Main) {
                    loadGraphTaskData()
                    // Reset dialog state after successful reset
                    completionDialogShowing = false
                    kotlinx.coroutines.delay(100)
                    isResetting = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting task", e)
                isResetting = false
            }
        }
    }

    private fun loadGraphTaskData() {
        viewModel.loadGraphTask(args.taskId)
    }

    private fun observeGraphTaskData() {
        viewModel.graphTask.observe(viewLifecycleOwner) { graphTaskWithSteps ->
            if (!isAdded) return@observe

            graphTaskWithSteps?.let { task ->
                Log.d(TAG, "Observing graph task: ${task.task.title}")
                Log.d(TAG, "Steps count: ${task.steps.size}")
                // Add detailed step logging
                task.steps.forEachIndexed { index, step ->
                    Log.d(TAG, "Step $index: ${step.description}, Order: ${step.orderNumber}")
                }

                binding.apply {
                    textViewTitle.text = task.task.title
                    textViewDescription.text = task.task.description

                    // Add logging before submitting to adapter
                    Log.d(TAG, "Submitting ${task.steps.size} steps to adapter")
                    val sortedSteps = task.steps.sortedBy { it.orderNumber }
                    Log.d(TAG, "Sorted steps count: ${sortedSteps.size}")
                    stepAdapter.submitList(sortedSteps)
                }
            } ?: run {
                Log.e(TAG, "No task data available")
                Snackbar.make(binding.root, "Error loading task", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun navigateToEdit() {
        try {
            viewModel.graphTask.value?.let { graphTaskWithSteps ->
                val steps = graphTaskWithSteps.steps
                    .sortedBy { it.orderNumber }
                    .map { it.description }
                    .toTypedArray()

                val gratificationSteps = graphTaskWithSteps.steps
                    .filter { it.isGratification }
                    .map { it.orderNumber }
                    .toIntArray()

                val action = ViewSingleGraphTaskFragmentDirections
                    .actionNavigationViewSingleGraphTaskToNavigationEditGraphTask(
                        taskId = args.taskId,
                        taskTitle = binding.textViewTitle.text.toString(),
                        taskDescription = binding.textViewDescription.text.toString(),
                        steps = steps,
                        gratificationSteps = gratificationSteps
                    )
                findNavController().navigate(action)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to edit screen", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        completionDialogShowing = false
        isResetting = false
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        completionDialogShowing = false
        isResetting = false
    }

    companion object {
        private const val TAG = "ViewSingleGraphTaskFrag"
    }
}