package com.sharc.ramdhd.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.DialogIconSelectorBinding

class IconSelectorDialog : DialogFragment() {
    private var _binding: DialogIconSelectorBinding? = null
    private val binding get() = _binding!!
    private var onIconSelectedListener: ((IconOption) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIconSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = IconCategoryAdapter(this)

        // Customize tab layout before attaching mediator
        binding.tabLayout.apply {
            tabMode = TabLayout.MODE_SCROLLABLE  // Changed from AUTO to SCROLLABLE
            isTabIndicatorFullWidth = false
        }

        // Create tab mediator with custom configuration
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = iconCategories[position].categoryName
            tab.view.minimumWidth = resources.getDimensionPixelSize(R.dimen.tab_min_width)  // Optional: define in dimens.xml
        }.attach()
    }

    fun setOnIconSelectedListener(listener: (IconOption) -> Unit) {
        onIconSelectedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class IconCategoryAdapter(fragment: DialogFragment) :
        FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = iconCategories.size
        override fun createFragment(position: Int): Fragment =
            IconGridFragment.newInstance(position) { iconOption ->
                onIconSelectedListener?.invoke(iconOption)
                dismiss()
            }
    }

    data class IconCategory(
        val categoryName: String,
        val icons: List<IconOption>
    )

    data class IconOption(
        val icon: String,
        val description: String
    )

    companion object {
        val iconCategories = listOf(
            IconCategory("Common", listOf(
                IconOption("📝", "Write"),
                IconOption("✅", "Check"),
                IconOption("❌", "Cross"),
                IconOption("⭐", "Star"),
                IconOption("💫", "Sparkles"),
                IconOption("🔍", "Search"),
                IconOption("📌", "Pin"),
                IconOption("🎯", "Target"),
                IconOption("❤️", "Heart"),
                IconOption("💎", "Diamond"),
                IconOption("🔔", "Bell"),
                IconOption("✨", "Sparkle")
            )),

            IconCategory("Food", listOf(
                IconOption("☕", "Coffee"),
                IconOption("🍵", "Tea"),
                IconOption("🥤", "Drink"),
                IconOption("🍎", "Apple"),
                IconOption("🍕", "Pizza"),
                IconOption("🍔", "Burger"),
                IconOption("🍦", "Ice Cream"),
                IconOption("🍪", "Cookie"),
                IconOption("🍩", "Donut"),
                IconOption("🥕", "Carrot"),
                IconOption("🥑", "Avocado"),
                IconOption("🍗", "Chicken"),
                IconOption("🥩", "Meat"),
                IconOption("🥗", "Salad"),
                IconOption("🍱", "Bento")
            )),

            IconCategory("Emotions", listOf(
                IconOption("😊", "Smile"),
                IconOption("😎", "Cool"),
                IconOption("🤔", "Thinking"),
                IconOption("😴", "Sleep"),
                IconOption("🤓", "Nerd"),
                IconOption("🥳", "Party"),
                IconOption("😇", "Angel"),
                IconOption("🤗", "Hug"),
                IconOption("🤭", "Giggle"),
                IconOption("😌", "Relieved"),
                IconOption("😃", "Happy"),
                IconOption("🤪", "Silly"),
                IconOption("😍", "Love"),
                IconOption("🤩", "Star Eyes"),
                IconOption("😋", "Yummy")
            )),

            IconCategory("Animals", listOf(
                IconOption("🐶", "Dog"),
                IconOption("🐱", "Cat"),
                IconOption("🦁", "Lion"),
                IconOption("🐼", "Panda"),
                IconOption("🦊", "Fox"),
                IconOption("🦋", "Butterfly"),
                IconOption("🐘", "Elephant"),
                IconOption("🦒", "Giraffe"),
                IconOption("🐧", "Penguin"),
                IconOption("🦉", "Owl"),
                IconOption("🦄", "Unicorn"),
                IconOption("🐬", "Dolphin"),
                IconOption("🦜", "Parrot"),
                IconOption("🐢", "Turtle"),
                IconOption("🦈", "Shark")
            )),

            IconCategory("Nature", listOf(
                IconOption("🌸", "Flower"),
                IconOption("🌺", "Hibiscus"),
                IconOption("🌹", "Rose"),
                IconOption("🌷", "Tulip"),
                IconOption("🌻", "Sunflower"),
                IconOption("🌳", "Tree"),
                IconOption("🌴", "Palm"),
                IconOption("🌵", "Cactus"),
                IconOption("🍀", "Clover"),
                IconOption("🌿", "Herb"),
                IconOption("🍁", "Maple"),
                IconOption("🍂", "Fallen Leaf"),
                IconOption("🌎", "Earth"),
                IconOption("🌞", "Sun"),
                IconOption("⭐", "Star")
            )),

            IconCategory("Weather", listOf(
                IconOption("☀️", "Sun"),
                IconOption("🌤️", "Sun & Cloud"),
                IconOption("☁️", "Cloud"),
                IconOption("🌧️", "Rain"),
                IconOption("⛈️", "Storm"),
                IconOption("❄️", "Snow"),
                IconOption("🌈", "Rainbow"),
                IconOption("⚡", "Lightning"),
                IconOption("🌪️", "Tornado"),
                IconOption("🌊", "Wave"),
                IconOption("💨", "Wind"),
                IconOption("☔", "Umbrella"),
                IconOption("⛱️", "Beach Umbrella"),
                IconOption("🌡️", "Thermometer"),
                IconOption("🌝", "Full Moon")
            )),

            IconCategory("Progress", listOf(
                IconOption("📈", "Growth"),
                IconOption("📊", "Chart"),
                IconOption("🎯", "Target"),
                IconOption("🏆", "Trophy"),
                IconOption("🏅", "Medal"),
                IconOption("🎖️", "Military Medal"),
                IconOption("👑", "Crown"),
                IconOption("💪", "Strong"),
                IconOption("🚀", "Rocket"),
                IconOption("🎨", "Palette"),
                IconOption("✍️", "Writing"),
                IconOption("📚", "Books"),
                IconOption("🎓", "Graduate"),
                IconOption("🌱", "Seedling"),
                IconOption("🌳", "Tree")
            )),

            IconCategory("Fun", listOf(
                IconOption("🎮", "Game"),
                IconOption("🎨", "Art"),
                IconOption("🎵", "Music"),
                IconOption("🎬", "Movie"),
                IconOption("🎲", "Dice"),
                IconOption("🎪", "Circus"),
                IconOption("🎭", "Theater"),
                IconOption("🎸", "Guitar"),
                IconOption("🎹", "Piano"),
                IconOption("🎯", "Darts"),
                IconOption("🎳", "Bowling"),
                IconOption("⚽", "Soccer"),
                IconOption("🏀", "Basketball"),
                IconOption("🎱", "Pool"),
                IconOption("🎰", "Slot Machine")
            )),

            IconCategory("Travel", listOf(
                IconOption("✈️", "Airplane"),
                IconOption("🚗", "Car"),
                IconOption("🚲", "Bike"),
                IconOption("🚂", "Train"),
                IconOption("🚢", "Ship"),
                IconOption("🚁", "Helicopter"),
                IconOption("🛵", "Scooter"),
                IconOption("🚕", "Taxi"),
                IconOption("🚌", "Bus"),
                IconOption("🚠", "Cable Car"),
                IconOption("🛳️", "Cruise Ship"),
                IconOption("🚄", "Fast Train"),
                IconOption("🛩️", "Small Plane"),
                IconOption("🚎", "Trolleybus"),
                IconOption("🛴", "Kick Scooter")
            )),

            IconCategory("Tools", listOf(
                IconOption("🛠️", "Tools"),
                IconOption("🔧", "Wrench"),
                IconOption("🔨", "Hammer"),
                IconOption("⚒️", "Hammer and Pick"),
                IconOption("🪛", "Screwdriver"),
                IconOption("⚡", "Lightning"),
                IconOption("💡", "Idea"),
                IconOption("✂️", "Scissors"),
                IconOption("📏", "Ruler"),
                IconOption("🔑", "Key"),
                IconOption("🔐", "Lock"),
                IconOption("📎", "Clip"),
                IconOption("🗑️", "Trash"),
                IconOption("📦", "Package"),
                IconOption("🔋", "Battery")
            )),

            IconCategory("Tech", listOf(
                IconOption("💻", "Computer"),
                IconOption("📱", "Phone"),
                IconOption("⌨️", "Keyboard"),
                IconOption("🖥️", "Desktop"),
                IconOption("🖱️", "Mouse"),
                IconOption("🎮", "Game Controller"),
                IconOption("🔌", "Power"),
                IconOption("📡", "Satellite"),
                IconOption("🖨️", "Printer"),
                IconOption("💾", "Save"),
                IconOption("📼", "Video"),
                IconOption("🎧", "Headphones"),
                IconOption("🔊", "Speaker"),
                IconOption("📸", "Camera"),
                IconOption("🎥", "Movie Camera")
            )),

            IconCategory("Time", listOf(
                IconOption("⏰", "Alarm"),
                IconOption("⌚", "Watch"),
                IconOption("⏳", "Hourglass"),
                IconOption("⌛", "Timer"),
                IconOption("📅", "Calendar"),
                IconOption("🗓️", "Spiral Calendar"),
                IconOption("📆", "Tear-off Calendar"),
                IconOption("🕐", "Clock1"),
                IconOption("🕑", "Clock2"),
                IconOption("🕒", "Clock3"),
                IconOption("🕓", "Clock4"),
                IconOption("🕔", "Clock5"),
                IconOption("🌅", "Sunrise"),
                IconOption("🌙", "Moon"),
                IconOption("☀️", "Sun")
            ))
        )
    }
}