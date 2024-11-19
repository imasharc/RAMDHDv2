package com.sharc.ramdhd.ui.dashboard.graphTasks.editSingle

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.model.graphTask.GraphTaskWithSteps
import kotlinx.coroutines.launch

class EditGraphTaskFragment : Fragment() {
    companion object {
        private const val TAG = "EditGraphTaskFragment"
        private const val MIN_STEPS = 3
    }

    private val args: EditGraphTaskFragmentArgs by navArgs()
    private val viewModel: EditGraphTaskViewModel by viewModels()
    private lateinit var stepsContainer: LinearLayout
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private var isInitialLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_edit_graph_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called with taskId: ${args.taskId}")

        setupViews(view)
        setupObservers()
        loadInitialData()
    }

    private fun setupViews(view: View) {
        Log.d(TAG, "Setting up views")
        stepsContainer = view.findViewById(R.id.stepsContainer)
        titleInput = view.findViewById(R.id.titleInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)

        // Set initial values from arguments
        titleInput.setText(args.taskTitle)
        descriptionInput.setText(args.taskDescription)
        Log.d(TAG, "Initial values set - Title: ${args.taskTitle}, Description: ${args.taskDescription}")

        // Setup save button
        view.findViewById<View>(R.id.saveButton).setOnClickListener {
            saveGraphTask()
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "Setting up observers")
        viewModel.getSteps().observe(viewLifecycleOwner) { steps ->
            Log.d(TAG, "Steps observer triggered, steps count: ${steps.size}")
            if (isInitialLoad) {
                Log.d(TAG, "Processing initial load of steps")
                isInitialLoad = false
                updateStepFields(steps)
            }
        }
    }

    private fun loadInitialData() {
        Log.d(TAG, "Loading initial data, taskId: ${args.taskId}")
        viewModel.setTaskId(args.taskId)
        viewModel.initializeSteps(
            steps = args.steps,
            gratificationSteps = args.gratificationSteps
        )

        if (args.taskId == -1) {
            titleInput.requestFocus()
            showKeyboardFor(titleInput)
        }
    }

    private fun updateStepFields(steps: List<String>) {
        Log.d(TAG, "Updating step fields with ${steps.size} steps")
        stepsContainer.removeAllViews()

        steps.forEachIndexed { index, stepText ->
            Log.d(TAG, "Adding step $index: $stepText")
            addStepField(index, stepText)
        }

        if (steps.isNotEmpty()) {
            Log.d(TAG, "Adding extra empty step field at index ${steps.size}")
            addStepField(steps.size)
        }
    }

    private fun addStepField(index: Int, initialText: String = "") {
        try {
            Log.d(TAG, "Adding step field at index $index with text: $initialText")

            // Create horizontal container for step
            val stepContainer = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.design_margin)
                }
                orientation = LinearLayout.HORIZONTAL
            }

            // Create step input field
            val stepInput = EditText(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,  // 0 width with weight will make it fill remaining space
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f  // weight of 1
                )
                hint = "Step ${index + 1}"
                inputType = android.text.InputType.TYPE_CLASS_TEXT
                maxLines = 1
                minHeight = (48 * resources.displayMetrics.density).toInt()
                setPadding(
                    (8 * resources.displayMetrics.density).toInt(),
                    0,
                    (8 * resources.displayMetrics.density).toInt(),
                    0
                )
                imeOptions = EditorInfo.IME_ACTION_NEXT
                setText(initialText)

                setOnEditorActionListener { _, actionId, event ->
                    handleEditorAction(actionId, event, index)
                }

                setOnFocusChangeListener { _, hasFocus ->
                    handleFocusChange(hasFocus, index)
                }

                addTextChangedListener {
                    viewModel.updateStep(index, it?.toString() ?: "")
                }
            }

            // Create gratification checkbox
            val gratificationCheckbox = CheckBox(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = (8 * resources.displayMetrics.density).toInt()
                }
                text = "Gratification"
                isChecked = args.gratificationSteps?.contains(index) == true

                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateStepGratification(index, isChecked)
                }
            }

            stepContainer.addView(stepInput)
            stepContainer.addView(gratificationCheckbox)
            stepsContainer.addView(stepContainer)

            Log.d(TAG, "Successfully added step field at index $index")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding step field: ${e.message}", e)
        }
    }

    private fun handleEditorAction(actionId: Int, event: android.view.KeyEvent?, index: Int): Boolean {
        Log.d(TAG, "Handling editor action at index $index, actionId: $actionId")
        if (actionId == EditorInfo.IME_ACTION_NEXT ||
            (event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                    event.action == android.view.KeyEvent.ACTION_DOWN)) {

            // Get the LinearLayout container for current step
            val currentStepContainer = stepsContainer.getChildAt(index) as? LinearLayout
            // Get the EditText from the container
            val currentStep = currentStepContainer?.getChildAt(0) as? EditText

            if (currentStep?.text?.isNotEmpty() == true && index == stepsContainer.childCount - 1) {
                Log.d(TAG, "Adding new step field after current")
                addStepField(index + 1)
            }

            val nextIndex = index + 1
            // Get the next step's container and EditText
            val nextStepContainer = stepsContainer.getChildAt(nextIndex) as? LinearLayout
            val nextStepInput = nextStepContainer?.getChildAt(0) as? EditText
            nextStepInput?.requestFocus()
            return true
        }
        return false
    }

    private fun handleFocusChange(hasFocus: Boolean, index: Int) {
        Log.d(TAG, "Focus changed for step $index, hasFocus: $hasFocus")
        if (!hasFocus) {
            cleanupEmptySteps(index)
        } else if (index == stepsContainer.childCount - 1) {
            val stepContainer = stepsContainer.getChildAt(index) as? LinearLayout
            val currentStep = stepContainer?.getChildAt(0) as? EditText
            if (currentStep?.text?.isNotEmpty() == true) {
                Log.d(TAG, "Adding new step field after last field")
                addStepField(index + 1)
            }
        }
    }

    private fun cleanupEmptySteps(index: Int) {
        Log.d(TAG, "Cleaning up empty steps after index $index")
        post {
            try {
                val currentFocusContainer = stepsContainer.findFocus()?.parent as? LinearLayout
                val currentFocusIndex = stepsContainer.indexOfChild(currentFocusContainer)
                Log.d(TAG, "Current focus index: $currentFocusIndex")

                if (currentFocusIndex in 0 until index) {
                    var lastNonEmptyIndex = MIN_STEPS - 1
                    for (i in stepsContainer.childCount - 1 downTo MIN_STEPS) {
                        val stepContainer = stepsContainer.getChildAt(i) as? LinearLayout
                        val step = stepContainer?.getChildAt(0) as? EditText
                        if (step?.text?.isNotEmpty() == true) {
                            lastNonEmptyIndex = i
                            break
                        }
                    }

                    val keepUntilIndex = lastNonEmptyIndex + 1
                    Log.d(TAG, "Keeping steps until index: $keepUntilIndex")

                    var i = stepsContainer.childCount - 1
                    while (i > keepUntilIndex && i > currentFocusIndex + 1) {
                        val stepContainer = stepsContainer.getChildAt(i) as? LinearLayout
                        val step = stepContainer?.getChildAt(0) as? EditText
                        if (step?.text?.isEmpty() == true) {
                            Log.d(TAG, "Removing empty step at index $i")
                            stepsContainer.removeViewAt(i)
                        }
                        i--
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up empty steps: ${e.message}", e)
            }
        }
    }

    private fun saveGraphTask() {
        Log.d(TAG, "Saving graph task")
        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()

        lifecycleScope.launch {
            try {
                val savedTask = viewModel.saveGraphTask(title, description)
                Log.d(TAG, "Successfully saved graph task")
                handleSuccessfulSave(savedTask)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving graph task: ${e.message}", e)
                handleSaveError(e)
            }
        }
    }

    private fun handleSuccessfulSave(savedTask: GraphTaskWithSteps) {
        if (!isAdded) {
            Log.d(TAG, "Fragment not attached, skipping success dialog")
            return
        }

        val activity = requireActivity()
        activity.supportFragmentManager.popBackStack()

        MaterialAlertDialogBuilder(activity)
            .setTitle("Graph Task Saved Successfully")
            .setMessage(buildTaskMessage(savedTask))
            .setPositiveButton("OK", null)
            .show()
        Log.d(TAG, "Showed success dialog")
    }

    private fun handleSaveError(e: Exception) {
        if (!isAdded) {
            Log.d(TAG, "Fragment not attached, skipping error dialog")
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error Saving Graph Task")
            .setMessage("An error occurred: ${e.localizedMessage}")
            .setPositiveButton("OK", null)
            .show()
        Log.e(TAG, "Showed error dialog")
    }

    private fun buildTaskMessage(taskWithSteps: GraphTaskWithSteps): String {
        return buildString {
            append("Title: ${taskWithSteps.task.title}\n")
            append("Description: ${taskWithSteps.task.description}\n")
            append("Steps:\n")
            taskWithSteps.steps.forEachIndexed { index, step ->
                append("${index + 1}. ${step.description}")
                if (step.isFinishing) append(" (Finishing)")
                if (step.isGratification) append(" (Gratification)")
                append("\n")
            }
        }
    }

    private fun showKeyboardFor(editText: EditText) {
        editText.post {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun post(action: () -> Unit) {
        view?.post(action)
    }
}