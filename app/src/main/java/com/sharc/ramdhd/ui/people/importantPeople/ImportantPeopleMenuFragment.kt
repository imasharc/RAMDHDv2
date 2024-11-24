package com.sharc.ramdhd.ui.people.importantPeople

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentImportantPeopleMenuBinding

class ImportantPeopleMenuFragment : Fragment(R.layout.fragment_important_people_menu) {
    private var _binding: FragmentImportantPeopleMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImportantPeopleMenuViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportantPeopleMenuBinding.bind(view)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.addNewEventFAB.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_important_people_menu_to_navigation_edit_single_important_event)
        }
    }
}