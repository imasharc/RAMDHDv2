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

class EditRoutineFragment : Fragment() {
    companion object {
        private const val TAG = "EditRoutineFragment"
    }

    private val viewModel: EditRoutineViewModel by viewModels()
    private lateinit var stepsContainer: LinearLayout
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepsContainer = view.findViewById(R.id.stepsContainer)
        titleInput = view.findViewById(R.id.titleInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)

        // Initialize with 3 step fields
        for (i in 0..2) {
            addStepField(i)
        }

        // Focus the first input and show keyboard
        titleInput.requestFocus()
        showKeyboardFor(titleInput)

        // Log the routine data after clicking "Save" button
        view.findViewById<View>(R.id.saveButton).setOnClickListener {
            val routineData = viewModel.getRoutineData(
                titleInput.text.toString(),
                descriptionInput.text.toString()
            )
            Log.d(TAG, "Routine Data: $routineData")
            // Pop the fragment
            requireActivity().supportFragmentManager.popBackStack()
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

    private fun addStepField(index: Int) {
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
            minHeight = 48 * resources.displayMetrics.density.toInt()
            setPadding(
                8 * resources.displayMetrics.density.toInt(),
                0,
                8 * resources.displayMetrics.density.toInt(),
                0
            )
            imeOptions = EditorInfo.IME_ACTION_NEXT

            setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                            event.action == android.view.KeyEvent.ACTION_DOWN)) {

                    // Always add a new step if pressing next on the last field
                    if (index == stepsContainer.childCount - 1) {
                        addStepField(index + 1)
                    }

                    // Focus the next field
                    val nextIndex = index + 1
                    val nextStepInput = stepsContainer.getChildAt(nextIndex) as? EditText
                    nextStepInput?.requestFocus()
                    true
                } else {
                    false
                }
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Add new step when focusing the last one
                    if (index == stepsContainer.childCount - 1) {
                        addStepField(index + 1)
                    }
                } else {
                    // When losing focus, check if we need to remove steps
                    post {
                        val currentFocus = stepsContainer.findFocus() as? EditText
                        val currentFocusIndex = if (currentFocus != null) {
                            stepsContainer.indexOfChild(currentFocus)
                        } else {
                            -1
                        }

                        // Only remove steps if we're moving to a previous step
                        if (currentFocusIndex in 0 until index) {
                            // Find the last non-empty step
                            var lastNonEmptyIndex = 2 // minimum of 3 steps
                            for (i in stepsContainer.childCount - 1 downTo 3) {
                                val step = stepsContainer.getChildAt(i) as EditText
                                if (!step.text.isNullOrEmpty()) {
                                    lastNonEmptyIndex = i
                                    break
                                }
                            }

                            // Keep the next step after the last non-empty one
                            val keepUntilIndex = lastNonEmptyIndex + 1

                            // Remove empty steps but keep one after the current focus
                            // and one after the last written step
                            var i = stepsContainer.childCount - 1
                            while (i > keepUntilIndex && i > currentFocusIndex + 1) {
                                val step = stepsContainer.getChildAt(i) as EditText
                                if (step.text.isNullOrEmpty()) {
                                    stepsContainer.removeViewAt(i)
                                }
                                i--
                            }
                        }
                    }
                }
            }

            addTextChangedListener {
                viewModel.updateStep(index, it.toString())
            }
        }

        stepsContainer.addView(stepInput)
    }
}