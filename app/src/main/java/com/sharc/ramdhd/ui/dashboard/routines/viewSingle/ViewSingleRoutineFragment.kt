package com.sharc.ramdhd.ui.dashboard.routines.viewSingle

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
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.repository.RoutineRepository
import com.sharc.ramdhd.databinding.FragmentViewSingleRoutineBinding
import com.sharc.ramdhd.routineDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewSingleRoutineFragment : Fragment(R.layout.fragment_view_single_routine) {
    private var _binding: FragmentViewSingleRoutineBinding? = null
    private val binding get() = _binding!!
    private val args: ViewSingleRoutineFragmentArgs by navArgs()
    private lateinit var stepAdapter: StepAdapter
    private var completionDialogShowing = false
    private var isResetting = false

    private val repository by lazy {
        RoutineRepository(requireContext().routineDao)
    }

    private val viewModel: ViewSingleRoutineViewModel by viewModels {
        ViewSingleRoutineViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentViewSingleRoutineBinding.bind(view)
        setupRecyclerView()
        setupEditFab()
        loadRoutineData()
        observeRoutineData()
    }

    private fun setupRecyclerView() {
        stepAdapter = StepAdapter().apply {
            setOnStepCheckedListener { stepId, isChecked ->
                if (isAdded && !completionDialogShowing && !isResetting) {
                    viewModel.handleStepStateChange(
                        stepId = stepId,
                        isChecked = isChecked,
                        routineId = args.routineId
                    ) { isCompleted ->
                        if (isCompleted) {
                            stepAdapter.checkCompletionState()
                        } else {
                            viewModel.markRoutineAsNotCompleted(args.routineId)
                        }
                    }
                }
            }

            setOnAllStepsCheckedListener {
                if (isAdded && !completionDialogShowing && !isResetting) {
                    showCompletionDialog()
                }
            }

            setOnNotAllStepsCheckedListener {
                if (isAdded && !isResetting) {
                    viewModel.markRoutineAsNotCompleted(args.routineId)
                }
            }
        }

        binding.recyclerViewSteps.apply {
            adapter = stepAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun loadRoutineData() {
        viewModel.loadRoutine(args.routineId)
    }

    private fun observeRoutineData() {
        viewModel.routine.observe(viewLifecycleOwner) { routineWithSteps ->
            if (!isAdded) return@observe

            Log.d("Fragment", "Observing routine: ${routineWithSteps.routine.title}")
            Log.d("Fragment", "Steps count: ${routineWithSteps.steps.size}")

            binding.apply {
                textViewTitle.text = routineWithSteps.routine.title
                textViewDescription.text = routineWithSteps.routine.description

                val sortedSteps = routineWithSteps.steps.sortedBy { it.orderNumber }
                Log.d("Fragment", "Sorted steps: ${sortedSteps.size}")

                lifecycleScope.launch(Dispatchers.Main) {
                    if (isAdded) {
                        stepAdapter.submitList(sortedSteps)
                    }
                }
            }
        }
    }
    private fun setupEditFab() {
        binding.fabEdit.setOnClickListener {
            try {
                viewModel.routine.value?.let { routineWithSteps ->
                    val steps = routineWithSteps.steps
                        .sortedBy { it.orderNumber }
                        .map { it.description }
                        .toTypedArray()

                    Log.d(TAG, "Navigating to edit with steps: ${steps.joinToString()}")

                    val action = ViewSingleRoutineFragmentDirections
                        .actionNavigationViewSingleRoutineToNavigationEditRoutine(
                            routineId = routineWithSteps.routine.id,
                            routineTitle = routineWithSteps.routine.title,
                            routineDescription = routineWithSteps.routine.description,
                            steps = steps
                        )
                    findNavController().navigate(action)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to edit screen", e)
            }
        }
    }

    private fun showCompletionDialog() {
        if (!isAdded || completionDialogShowing || isResetting) return

        completionDialogShowing = true
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Congratulations!")
            .setMessage("Congratulations on completing this routine")
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
                completeRoutine()
            }
            .setNeutralButton("Reset") { dialog, _ ->
                dialog.dismiss()
                resetRoutine()
            }
            .setOnDismissListener {
                completionDialogShowing = false
            }
            .setCancelable(false)
            .show()
    }

    private fun completeRoutine() {
        if (!isAdded) return

        viewModel.markRoutineAsCompleted(args.routineId)

        try {
            findNavController().navigateUp()
        } catch (e: Exception) {
            Log.e("ViewSingleRoutineFragment", "Error during navigation", e)
        }
    }

    private fun resetRoutine() {
        if (!isAdded) return

        isResetting = true
        lifecycleScope.launch {
            try {
                viewModel.resetRoutine(args.routineId)
                // Wait for the reset to complete
                withContext(Dispatchers.Main) {
                    loadRoutineData()
                    // Reset the flag after a short delay to ensure the UI has updated
                    kotlinx.coroutines.delay(100)
                    isResetting = false
                }
            } catch (e: Exception) {
                Log.e("ViewSingleRoutineFragment", "Error resetting routine", e)
                isResetting = false
            }
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
}