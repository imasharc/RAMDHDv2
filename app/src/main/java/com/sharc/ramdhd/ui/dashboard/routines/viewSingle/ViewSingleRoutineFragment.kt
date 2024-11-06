package com.sharc.ramdhd.ui.dashboard.routines.viewSingle

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.dao.RoutineDao
import com.sharc.ramdhd.data.repository.RoutineRepository
import com.sharc.ramdhd.databinding.FragmentViewSingleRoutineBinding
import com.sharc.ramdhd.routineDao

class ViewSingleRoutineFragment : Fragment(R.layout.fragment_view_single_routine) {
    private var _binding: FragmentViewSingleRoutineBinding? = null
    private val binding get() = _binding!!
    private val args: ViewSingleRoutineFragmentArgs by navArgs()
    private lateinit var stepAdapter: StepAdapter

    // Get repository instance
    private val repository by lazy {
        RoutineRepository(requireContext().routineDao)
    }

    private val viewModel: ViewSingleRoutineViewModel by viewModels {
        ViewSingleRoutineViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentViewSingleRoutineBinding.bind(view)

        // Show initial data from arguments
        binding.textViewTitle.text = args.routineTitle
        binding.textViewDescription.text = args.routineDescription

        setupRecyclerView()
        loadRoutineData()
        observeRoutineData()
    }

    private fun setupRecyclerView() {
        stepAdapter = StepAdapter()
        binding.recyclerViewSteps.apply {
            adapter = stepAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            // Disable nested scrolling
            isNestedScrollingEnabled = false
        }
    }

    private fun loadRoutineData() {
        viewModel.loadRoutine(args.routineId)
    }

    private fun observeRoutineData() {
        viewModel.routine.observe(viewLifecycleOwner) { routineWithSteps ->
            Log.d("Fragment", "Observing routine: ${routineWithSteps.routine.title}")
            Log.d("Fragment", "Steps count: ${routineWithSteps.steps.size}")
            binding.apply {
                textViewTitle.text = routineWithSteps.routine.title
                textViewDescription.text = routineWithSteps.routine.description
                val sortedSteps = routineWithSteps.steps.sortedBy { it.orderNumber }
                Log.d("Fragment", "Sorted steps: ${sortedSteps.size}")
                stepAdapter.submitList(sortedSteps)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}