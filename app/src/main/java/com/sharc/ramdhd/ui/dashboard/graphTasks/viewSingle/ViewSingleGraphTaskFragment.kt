package com.sharc.ramdhd.ui.dashboard.graphTasks.viewSingle

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.database.AppDatabase
import com.sharc.ramdhd.data.repository.GraphTaskRepository
import com.sharc.ramdhd.databinding.FragmentViewSingleGraphTaskBinding

class ViewSingleGraphTaskFragment : Fragment(R.layout.fragment_view_single_graph_task) {
    private var _binding: FragmentViewSingleGraphTaskBinding? = null
    private val binding get() = _binding!!
    private val args: ViewSingleGraphTaskFragmentArgs by navArgs()

    // Add stepAdapter as a class property
    private lateinit var stepAdapter: ViewGraphStepAdapter

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
    }

    private fun setupViews() {
        stepAdapter = ViewGraphStepAdapter()
        binding.recyclerViewSteps.apply {
            adapter = stepAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        binding.fabEdit.setOnClickListener {
            // Your edit navigation code
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

                binding.apply {
                    textViewTitle.text = task.task.title
                    textViewDescription.text = task.task.description
                    // Submit the sorted list of steps to the adapter
                    stepAdapter.submitList(task.steps.sortedBy { it.orderNumber })
                }
            } ?: run {
                Log.e(TAG, "No task data available")
                Snackbar.make(binding.root, "Error loading task", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ViewSingleGraphTaskFrag"
    }
}