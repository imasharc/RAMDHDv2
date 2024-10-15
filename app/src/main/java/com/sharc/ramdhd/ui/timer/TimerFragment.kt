package com.sharc.ramdhd.ui.timer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sharc.ramdhd.R
import com.sharc.ramdhd.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {
    private var _binding: FragmentTimerBinding? = null
    private val viewModel: TimerViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var notificationManager: NotificationManager
    private lateinit var vibrator: Vibrator

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, you can show notifications
        } else {
            // Permission denied, inform the user that they won't receive notifications
            Toast.makeText(requireContext(), "Notifications are disabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        setupNotificationChannel()
        setupNumberPickers()
        setupButtons()
        observeViewModel()

        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        return binding.root
    }

    private fun setupNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timer Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupNumberPickers() {
        binding.hourPicker.apply {
            minValue = 0
            maxValue = 23
            setOnValueChangedListener { _, _, newVal ->
                viewModel.setHours(newVal)
            }
        }

        binding.minutePicker.apply {
            minValue = 0
            maxValue = 59
            setOnValueChangedListener { _, _, newVal ->
                viewModel.setMinutes(newVal)
            }
        }

        binding.secondPicker.apply {
            minValue = 0
            maxValue = 59
            setOnValueChangedListener { _, _, newVal ->
                viewModel.setSeconds(newVal)
            }
        }
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            viewModel.startTimer()
            checkNotificationPermission()
        }
        binding.stopButton.setOnClickListener { viewModel.stopTimer() }
        binding.resetButton.setOnClickListener { viewModel.resetTimer() }
    }

    private fun observeViewModel() {
        viewModel.timerText.observe(viewLifecycleOwner) { timeString ->
            binding.timerTextView.text = timeString
        }

        viewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            binding.startButton.isEnabled = !isRunning
            binding.stopButton.isEnabled = isRunning
            binding.hourPicker.isEnabled = !isRunning
            binding.minutePicker.isEnabled = !isRunning
            binding.secondPicker.isEnabled = !isRunning
        }

        viewModel.canStart.observe(viewLifecycleOwner) { canStart ->
            binding.startButton.isEnabled = canStart && !viewModel.isRunning.value!!
            binding.resetButton.isEnabled = !viewModel.isRunning.value!!
        }

        viewModel.timerFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                showNotification()
                vibrateDevice()
                viewModel.onTimerFinishedHandled()
            }
        }

        viewModel.resetPickers.observe(viewLifecycleOwner) { reset ->
            if (reset) {
                binding.hourPicker.value = 0
                binding.minutePicker.value = 0
                binding.secondPicker.value = 0
                viewModel.onResetPickersHandled()
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is granted, we can show notifications
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain to the user why we need the permission
                    Toast.makeText(requireContext(), "Notification permission is required for timer alerts", Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, don't show the notification
            Toast.makeText(requireContext(), "Timer finished, but notifications are disabled", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Timer Finished")
            .setContentText("Your timer has ended!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun vibrateDevice() {
        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val CHANNEL_ID = "TimerChannel"
        private const val NOTIFICATION_ID = 1
    }
}