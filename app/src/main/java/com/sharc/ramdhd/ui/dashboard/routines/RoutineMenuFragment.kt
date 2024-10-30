package com.sharc.ramdhd.ui.dashboard.routines

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentRoutineMenuBinding
import com.sharc.ramdhd.ui.dashboard.routines.editSingle.EditRoutineViewModel

class RoutineMenuFragment : Fragment() {

    private var _binding: FragmentRoutineMenuBinding? = null
    private val viewModel: EditRoutineViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineMenuBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set click listener for the add routine FAB
        binding.myImageView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_routine_menu_to_navigation_edit_routine)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}