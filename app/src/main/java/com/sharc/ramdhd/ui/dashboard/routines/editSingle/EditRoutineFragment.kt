package com.sharc.ramdhd.ui.dashboard.routines.editSingle

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.core.widget.addTextChangedListener
import com.sharc.ramdhd.R
import android.widget.LinearLayout
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharc.ramdhd.data.model.RoutineWithSteps
import kotlinx.coroutines.launch

class EditRoutineFragment : Fragment() {
    companion object {
        private const val TAG = "EditRoutineFragment"
        private const val MIN_STEPS = 3
    }

    private val args: EditRoutineFragmentArgs by navArgs()
    private val viewModel: EditRoutineViewModel by viewModels()
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
        return inflater.inflate(R.layout.fragment_edit_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called with routineId: ${args.routineId}")

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
        titleInput.setText(args.routineTitle)
        descriptionInput.setText(args.routineDescription)
        Log.d(TAG, "Initial values set - Title: ${args.routineTitle}, Description: ${args.routineDescription}")

        // Setup save button
        view.findViewById<View>(R.id.saveButton).setOnClickListener {
            saveRoutine()
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
        if (args.routineId != -1) {
            Log.d(TAG, "Loading existing routine with ID: ${args.routineId}")
            viewModel.loadRoutine(args.routineId)
        } else {
            Log.d(TAG, "Initializing new routine with $MIN_STEPS steps")
            for (i in 0 until MIN_STEPS) {
                addStepField(i)
            }
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
            val stepInput = EditText(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.design_margin)
                }
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
            stepsContainer.addView(stepInput)
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

            val currentStep = stepsContainer.getChildAt(index) as EditText
            if (!currentStep.text.isNullOrEmpty() && index == stepsContainer.childCount - 1) {
                Log.d(TAG, "Adding new step field after current")
                addStepField(index + 1)
            }

            val nextIndex = index + 1
            val nextStepInput = stepsContainer.getChildAt(nextIndex) as? EditText
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
            val currentStep = stepsContainer.getChildAt(index) as EditText
            if (!currentStep.text.isNullOrEmpty()) {
                Log.d(TAG, "Adding new step field after last field")
                addStepField(index + 1)
            }
        }
    }

    private fun cleanupEmptySteps(index: Int) {
        Log.d(TAG, "Cleaning up empty steps after index $index")
        post {
            try {
                val currentFocus = stepsContainer.findFocus() as? EditText
                val currentFocusIndex = stepsContainer.indexOfChild(currentFocus)
                Log.d(TAG, "Current focus index: $currentFocusIndex")

                if (currentFocusIndex in 0 until index) {
                    var lastNonEmptyIndex = MIN_STEPS - 1
                    for (i in stepsContainer.childCount - 1 downTo MIN_STEPS) {
                        val step = stepsContainer.getChildAt(i) as EditText
                        if (!step.text.isNullOrEmpty()) {
                            lastNonEmptyIndex = i
                            break
                        }
                    }

                    val keepUntilIndex = lastNonEmptyIndex + 1
                    Log.d(TAG, "Keeping steps until index: $keepUntilIndex")

                    var i = stepsContainer.childCount - 1
                    while (i > keepUntilIndex && i > currentFocusIndex + 1) {
                        val step = stepsContainer.getChildAt(i) as EditText
                        if (step.text.isNullOrEmpty()) {
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

    private fun saveRoutine() {
        Log.d(TAG, "Saving routine")
        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()

        lifecycleScope.launch {
            try {
                val savedRoutine = viewModel.saveRoutine(title, description)
                Log.d(TAG, "Successfully saved routine")
                handleSuccessfulSave(savedRoutine)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving routine: ${e.message}", e)
                handleSaveError(e)
            }
        }
    }

    private fun handleSuccessfulSave(savedRoutine: RoutineWithSteps) {
        if (!isAdded) {
            Log.d(TAG, "Fragment not attached, skipping success dialog")
            return
        }

        val activity = requireActivity()
        activity.supportFragmentManager.popBackStack()

        MaterialAlertDialogBuilder(activity)
            .setTitle("Routine Saved Successfully")
            .setMessage(buildRoutineMessage(savedRoutine))
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
            .setTitle("Error Saving Routine")
            .setMessage("An error occurred: ${e.localizedMessage}")
            .setPositiveButton("OK", null)
            .show()
        Log.e(TAG, "Showed error dialog")
    }

    private fun buildRoutineMessage(routineWithSteps: RoutineWithSteps): String {
        return buildString {
            append("Title: ${routineWithSteps.routine.title}\n")
            append("Description: ${routineWithSteps.routine.description}\n")
            append("Steps:\n")
            routineWithSteps.steps.forEachIndexed { index, step ->
                append("${index + 1}. ${step.description}\n")
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