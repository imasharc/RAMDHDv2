package com.sharc.ramdhd.ui.dashboard.graphTasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentGraphTaskMenuBinding

class GraphTaskMenuFragment : Fragment(R.layout.fragment_graph_task_menu) {
    private var _binding: FragmentGraphTaskMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GraphTaskMenuViewModel by viewModels()
//    private lateinit var graphTaskAdapter: GraphTaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphTaskMenuBinding.bind(view)
//        setupRecyclerView()
        setupClickListeners()
        setupBackPressHandler()
//        observeGraphTasks()
    }

//    private fun setupRecyclerView() {
//        graphTaskAdapter = GraphTaskAdapter()
//        binding.recyclerViewGraphTasks.apply {
//            adapter = graphTaskAdapter
//            layoutManager = LinearLayoutManager(requireContext())
//            setHasFixedSize(true)
//        }
//    }

    private fun setupClickListeners() {
        binding.addNewGraphTask.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_graph_task_menu_to_navigation_edit_graph_task)
        }
    }

    private fun setupBackPressHandler() {
        // TODO: Implement back press handling if needed
    }

//    private fun observeGraphTasks() {
//        viewModel.graphTasks.observe(viewLifecycleOwner) { tasks ->
//            graphTaskAdapter.submitList(tasks)
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = GraphTaskMenuFragment()
    }
}