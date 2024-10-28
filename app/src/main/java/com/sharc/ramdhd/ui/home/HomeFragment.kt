package com.sharc.ramdhd.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentHomeBinding
import androidx.fragment.app.viewModels

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set click listener for the timer panel
        binding.timerPanel.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_timer)
        }

        // Set click listener for the notes panel
        binding.notesPanelContainer.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_notes)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}