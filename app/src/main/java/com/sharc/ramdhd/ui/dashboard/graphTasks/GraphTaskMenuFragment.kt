package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphTaskMenuBinding.bind(view)

        setupClickListeners()
        observeGraphTasks()
    }

    private fun setupClickListeners() {
        binding.addNewGraphTask.setOnClickListener {
            findNavController().navigate(
                GraphTaskMenuFragmentDirections.actionNavigationGraphTaskMenuToNavigationEditGraphTask()
            )
        }
    }

    private fun observeGraphTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allGraphTasks.collectLatest { tasks ->
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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}