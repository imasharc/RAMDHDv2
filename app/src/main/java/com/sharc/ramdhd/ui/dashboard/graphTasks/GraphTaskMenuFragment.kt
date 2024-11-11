package com.sharc.ramdhd.ui.dashboard.graphTasks

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sharc.ramdhd.R

class GraphTaskMenuFragment : Fragment() {

    companion object {
        fun newInstance() = GraphTaskMenuFragment()
    }

    private val viewModel: GraphTaskMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_graph_task_menu, container, false)
    }
}