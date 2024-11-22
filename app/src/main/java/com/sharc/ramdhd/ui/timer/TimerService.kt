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
import android.provider.Settings
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

        // Initialize wake lock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TimerService::WakeLock"
        )

        // Initialize vibrator
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

        // Update finish channel to enable sound
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

    private fun showFinishNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, FINISH_CHANNEL_ID)
            .setContentTitle("Timer Finished!")
            .setContentText("Your timer has completed")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000))
            // Add alarm sound
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()

        notificationManager.notify(FINISH_NOTIFICATION_ID, notification)
        vibrateDevice()
    }

    fun startTimer(timeMillis: Long) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire(timeMillis + 1000)
        }

        Log.d(TAG, "Starting timer with $timeMillis ms")
        originalTimeMillis = timeMillis
        remainingTimeMillis = timeMillis
        lastTickTime = System.currentTimeMillis()

        if (canScheduleExactAlarms()) {
            scheduleAlarm(timeMillis)
        }

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
                cancelAlarm()
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()

        _timerState.value = TimerState.Running(remainingTimeMillis)
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            // For API levels below 31 (Android 12), exact alarms are allowed by default
            true
        }
    }

    private fun scheduleAlarm(timeMillis: Long) {
        val intent = Intent(this, TimerService::class.java).apply {
            action = ACTION_RESTART_TIMER
        }
        val pendingIntent = PendingIntent.getService(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 and above, check permission
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + timeMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled for ${timeMillis}ms from now (Android 12+)")
            } else {
                Log.d(TAG, "Cannot schedule exact alarms - permission not granted")
            }
        } else {
            // For Android 11 and below
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + timeMillis,
                pendingIntent
            )
            Log.d(TAG, "Alarm scheduled for ${timeMillis}ms from now (pre-Android 12)")
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(this, TimerService::class.java).apply {
            action = ACTION_RESTART_TIMER
        }
        val pendingIntent = PendingIntent.getService(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm cancelled")
    }

    fun stopTimer() {
        Log.d(TAG, "Stopping timer")
        countDownTimer?.cancel()
        cancelAlarm()
        _timerState.value = TimerState.Stopped(remainingTimeMillis)
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
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
        cancelAlarm()
        super.onDestroy()
        countDownTimer?.cancel()
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