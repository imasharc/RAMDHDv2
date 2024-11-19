package com.sharc.ramdhd.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sharc.ramdhd.databinding.FragmentIconGridBinding
import com.sharc.ramdhd.databinding.ItemIconBinding

class IconGridFragment : Fragment() {
    private var _binding: FragmentIconGridBinding? = null
    private val binding get() = _binding!!
    private var onIconSelected: ((IconSelectorDialog.IconOption) -> Unit)? = null
    private lateinit var adapter: IconAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIconGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = IconAdapter { iconOption ->
            onIconSelected?.invoke(iconOption)
        }

        binding.recyclerViewIcons.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = this@IconGridFragment.adapter
        }

        val categoryPosition = arguments?.getInt(ARG_CATEGORY_POSITION) ?: 0
        adapter.submitList(IconSelectorDialog.iconCategories[categoryPosition].icons)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY_POSITION = "category_position"

        fun newInstance(
            categoryPosition: Int,
            onIconSelected: (IconSelectorDialog.IconOption) -> Unit
        ): IconGridFragment {
            return IconGridFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CATEGORY_POSITION, categoryPosition)
                }
                this.onIconSelected = onIconSelected
            }
        }
    }

    private class IconAdapter(
        private val onIconSelected: (IconSelectorDialog.IconOption) -> Unit
    ) : ListAdapter<IconSelectorDialog.IconOption, IconAdapter.IconViewHolder>(IconDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
            val binding = ItemIconBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return IconViewHolder(binding)
        }

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            val icon = getItem(position)
            holder.bind(icon)
            holder.itemView.setOnClickListener { onIconSelected(icon) }
        }

        class IconViewHolder(
            private val binding: ItemIconBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(icon: IconSelectorDialog.IconOption) {
                binding.textViewIcon.apply {
                    text = icon.icon
                }
            }
        }

        private class IconDiffCallback : DiffUtil.ItemCallback<IconSelectorDialog.IconOption>() {
            override fun areItemsTheSame(
                oldItem: IconSelectorDialog.IconOption,
                newItem: IconSelectorDialog.IconOption
            ): Boolean = oldItem.icon == newItem.icon

            override fun areContentsTheSame(
                oldItem: IconSelectorDialog.IconOption,
                newItem: IconSelectorDialog.IconOption
            ): Boolean = oldItem == newItem
        }
    }
}