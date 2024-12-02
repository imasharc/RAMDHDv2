package com.sharc.ramdhd.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.sharc.ramdhd.MainActivity
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent
import com.sharc.ramdhd.data.model.importantPeople.RecurrenceType
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class EventNotificationManager(private val context: Context) {
    companion object {
        private const val TAG = "EventNotificationManager"
        private const val CHANNEL_ID = "important_events_channel"
        private const val CHANNEL_NAME = "Important Events"
        private const val CHANNEL_DESCRIPTION = "Notifications for important events"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESCRIPTION
                    setShowBadge(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel", e)
            }
        }
    }

    private fun handleExistingNotification(event: ImportantEvent) {
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            // Cancel existing notification
            notificationManager.cancel(event.id)
            Log.d(TAG, "Cancelled existing notification for event: ${event.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling existing notification", e)
        }
    }

    fun hasNotificationPermission(): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        Log.d(TAG, "Has notification permission: $hasPermission")
        return hasPermission
    }

    fun scheduleNotification(event: ImportantEvent) {
        Log.d(TAG, "Scheduling notification for event: ${event.id}")

        // Handle any existing notification first
        handleExistingNotification(event)

        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("event_${event.id}")

        val currentTime = LocalDateTime.now()
        val today = currentTime.toLocalDate()
        val eventDate = event.eventDate.toLocalDate()

        // For events today, show notification immediately
        if (eventDate.isEqual(today)) {
            Log.d(TAG, "Event is today, scheduling immediate notification")
            scheduleNotificationWork(event, 0)
            return
        }

        // For future events
        if (eventDate.isAfter(today)) {
            val notificationTime = event.eventDate.withHour(9).withMinute(0)
            val initialDelay = currentTime.until(notificationTime, java.time.temporal.ChronoUnit.MINUTES)
            Log.d(TAG, "Event is in future, scheduling for: $notificationTime (delay: $initialDelay minutes)")
            scheduleNotificationWork(event, initialDelay)
            return
        }

        // For past events, handle recurrence
        when (event.recurrenceType) {
            RecurrenceType.WEEKLY -> {
                val nextOccurrence = findNextOccurrence(event.eventDate, 7)
                Log.d(TAG, "Scheduling next weekly occurrence for: $nextOccurrence")
                scheduleRecurringNotification(event, nextOccurrence)
            }
            RecurrenceType.BIWEEKLY -> {
                val nextOccurrence = findNextOccurrence(event.eventDate, 14)
                Log.d(TAG, "Scheduling next biweekly occurrence for: $nextOccurrence")
                scheduleRecurringNotification(event, nextOccurrence)
            }
            RecurrenceType.MONTHLY -> {
                var nextOccurrence = event.eventDate
                while (nextOccurrence.isBefore(currentTime)) {
                    nextOccurrence = nextOccurrence.plusMonths(1)
                }
                Log.d(TAG, "Scheduling next monthly occurrence for: $nextOccurrence")
                scheduleRecurringNotification(event, nextOccurrence)
            }
            RecurrenceType.YEARLY -> {
                var nextOccurrence = event.eventDate
                while (nextOccurrence.isBefore(currentTime)) {
                    nextOccurrence = nextOccurrence.plusYears(1)
                }
                Log.d(TAG, "Scheduling next yearly occurrence for: $nextOccurrence")
                scheduleRecurringNotification(event, nextOccurrence)
            }
            RecurrenceType.NONE -> {
                if (eventDate.isEqual(today)) {
                    scheduleNotificationWork(event, 0)
                } else {
                    Log.d(TAG, "Event is in the past and non-recurring")
                }
            }
        }
    }

    private fun findNextOccurrence(startDate: LocalDateTime, daysInterval: Long): LocalDateTime {
        var nextDate = startDate
        val currentTime = LocalDateTime.now()
        while (nextDate.isBefore(currentTime)) {
            nextDate = nextDate.plusDays(daysInterval)
        }
        return nextDate
    }

    private fun scheduleRecurringNotification(event: ImportantEvent, nextOccurrence: LocalDateTime) {
        val currentTime = LocalDateTime.now()
        val notificationTime = nextOccurrence.withHour(9).withMinute(0)
        val initialDelay = currentTime.until(notificationTime, java.time.temporal.ChronoUnit.MINUTES)
        scheduleNotificationWork(event, initialDelay)
    }

    private fun scheduleNotificationWork(event: ImportantEvent, delayMinutes: Long) {
        val inputData = workDataOf(
            "event_id" to event.id,
            "event_type" to event.eventType.name,
            "event_name" to event.eventName,
            "person_name" to event.personName,
            "event_date" to event.eventDate.toString()
        )

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()

        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("event_${event.id}")
            .build()

        WorkManager.getInstance(context).enqueue(notificationWork)
        Log.d(TAG, "Work request enqueued for event ${event.id} with ${delayMinutes}min delay")

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(notificationWork.id)
            .observeForever { workInfo ->
                Log.d(TAG, "Work status for event ${event.id}: ${workInfo?.state}")
            }
    }

    class NotificationWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {

        override fun doWork(): Result {
            Log.d(TAG, "NotificationWorker started")

            setForegroundAsync(createForegroundInfo())

            val eventId = inputData.getInt("event_id", -1)
            val eventType = inputData.getString("event_type")
            val eventName = inputData.getString("event_name")
            val personName = inputData.getString("person_name")
            val eventDateStr = inputData.getString("event_date")

            if (eventId == -1 || eventType == null || eventName == null || personName == null || eventDateStr == null) {
                Log.e(TAG, "Missing required data. eventId: $eventId, type: $eventType, name: $eventName, person: $personName, date: $eventDateStr")
                return Result.failure()
            }

            // Check if notification should still be shown based on current date
            val eventDate = LocalDateTime.parse(eventDateStr)
            val currentDate = LocalDateTime.now().toLocalDate()
            if (eventDate.toLocalDate().isBefore(currentDate)) {
                Log.d(TAG, "Event date is in the past, skipping notification")
                return Result.success()
            }

            return try {
                showNotification(eventId, eventType, eventName, personName)
                Log.d(TAG, "Notification shown successfully for event $eventId")
                Result.success()
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception showing notification", e)
                Result.failure()
            } catch (e: Exception) {
                Log.e(TAG, "Error showing notification", e)
                Result.failure()
            }
        }

        private fun createForegroundInfo(): ForegroundInfo {
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Preparing event reminder")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            return ForegroundInfo(
                System.currentTimeMillis().toInt(),
                notification
            )
        }

        private fun showNotification(eventId: Int, eventType: String, eventName: String, personName: String) {
            Log.d(TAG, "Preparing to show notification for event $eventId")

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("event_id", eventId)
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("$eventType, $eventName - $personName")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    NotificationManagerCompat.from(applicationContext).notify(eventId, notification)
                    Log.d(TAG, "Notification posted successfully for event $eventId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error posting notification", e)
                    throw e
                }
            } else {
                Log.e(TAG, "No notification permission")
                throw SecurityException("No notification permission")
            }
        }
    }
}