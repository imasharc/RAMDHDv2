package com.sharc.ramdhd.ui.timer

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sharc.ramdhd.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TimerService : Service() {
    private var countDownTimer: CountDownTimer? = null
    private val binder = TimerBinder()
    private var originalTimeMillis: Long = 0
    private var remainingTimeMillis: Long = 0
    private var pausedTimeMillis: Long = 0
    private lateinit var wakeLock: PowerManager.WakeLock
    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private var lastTickTime: Long = 0
    private lateinit var vibrator: Vibrator

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Service bound")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with action: ${intent?.action}")

        if (intent?.action == ACTION_RESTART_TIMER && remainingTimeMillis > 0) {
            val currentTime = System.currentTimeMillis()
            val timePassed = currentTime - lastTickTime
            if (timePassed < remainingTimeMillis) {
                startTimer(remainingTimeMillis - timePassed)
            }
        }
        return START_STICKY
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TimerService::WakeLock"
        )

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun vibrateDevice() {
        vibrator.vibrate(VibrationEffect.createOneShot(3000, 255))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ONGOING_CHANNEL_ID,
            "Timer Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val finishChannel = NotificationChannel(
            FINISH_CHANNEL_ID,
            "Timer Finished",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(true)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
        }
        notificationManager.createNotificationChannel(finishChannel)
    }

    fun startTimer(timeMillis: Long) {
        // Use pausedTimeMillis if available, otherwise use the provided timeMillis
        val timeToUse = if (pausedTimeMillis > 0) pausedTimeMillis else timeMillis

        if (!wakeLock.isHeld) {
            wakeLock.acquire(timeToUse + 1000)
        }

        Log.d(TAG, "Starting timer with $timeToUse ms")
        originalTimeMillis = timeToUse
        remainingTimeMillis = timeToUse
        lastTickTime = System.currentTimeMillis()

        startForeground(ONGOING_NOTIFICATION_ID, createOngoingNotification(formatTime(remainingTimeMillis)))

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remainingTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                lastTickTime = System.currentTimeMillis()
                Log.d(TAG, "Timer tick: ${formatTime(millisUntilFinished)}")
                remainingTimeMillis = millisUntilFinished
                updateNotification(formatTime(millisUntilFinished))
                _timerState.value = TimerState.Running(millisUntilFinished)
            }

            override fun onFinish() {
                Log.d(TAG, "Timer finished")
                _timerState.value = TimerState.Finished
                showFinishNotification()
                pausedTimeMillis = 0  // Reset paused time when finished
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()

        // Reset paused time after starting
        pausedTimeMillis = 0
        _timerState.value = TimerState.Running(remainingTimeMillis)
    }

    fun stopTimer() {
        Log.d(TAG, "Stopping timer")
        countDownTimer?.cancel()
        // Save the remaining time when stopping
        pausedTimeMillis = remainingTimeMillis
        _timerState.value = TimerState.Stopped(remainingTimeMillis)
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        pausedTimeMillis = 0
        remainingTimeMillis = 0
        _timerState.value = TimerState.Idle
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showFinishNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, FINISH_CHANNEL_ID)
            .setContentTitle("Timer Finished!")
            .setContentText("Your timer has completed")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()

        notificationManager.notify(FINISH_NOTIFICATION_ID, notification)
        vibrateDevice()
    }

    private fun createOngoingNotification(timeString: String): android.app.Notification {
        val notificationIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ONGOING_CHANNEL_ID)
            .setContentTitle("Timer Running")
            .setContentText("Remaining: $timeString")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(timeString: String) {
        val notification = createOngoingNotification(timeString)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        countDownTimer?.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TimerService"
        private const val ONGOING_CHANNEL_ID = "TimerServiceChannel"
        private const val FINISH_CHANNEL_ID = "TimerFinishChannel"
        private const val ONGOING_NOTIFICATION_ID = 1
        private const val FINISH_NOTIFICATION_ID = 2
        private const val ACTION_RESTART_TIMER = "com.sharc.ramdhd.ACTION_RESTART_TIMER"
        private const val ALARM_REQUEST_CODE = 123
    }
}