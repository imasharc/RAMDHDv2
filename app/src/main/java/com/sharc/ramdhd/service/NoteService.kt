package com.sharc.ramdhd.service

import android.app.Notification  // Added this import
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.sharc.ramdhd.R
import com.sharc.ramdhd.data.model.Note
import java.time.LocalDateTime
import java.time.ZoneId

class NoteService : Service() {
    private val binder = NoteBinder()
    private lateinit var wakeLock: PowerManager.WakeLock
    private val channelId = "notes_channel"
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val activeNotes = mutableSetOf<Int>()

    inner class NoteBinder : Binder() {
        fun getService(): NoteService = this@NoteService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NoteService::WakeLock"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_SERVICE_ID,
                createBaseNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            )
        } else {
            startForeground(FOREGROUND_SERVICE_ID, createBaseNotification())
        }
    }

    private fun createBaseNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("You still have some notes remaining")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setTimeoutAfter(0)
            .build().apply {
                flags = flags or Notification.FLAG_NO_CLEAR or
                        Notification.FLAG_ONGOING_EVENT or
                        Notification.FLAG_FOREGROUND_SERVICE
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_NOTE -> {
                val noteId = intent.getIntExtra("note_id", -1)
                if (noteId != -1) {
                    stopNotification(noteId)
                }
            }
            else -> {
                val noteId = intent?.getIntExtra("note_id", -1) ?: -1
                val noteTitle = intent?.getStringExtra("note_title") ?: ""
                val noteDescription = intent?.getStringExtra("note_description") ?: ""

                if (noteId != -1) {
                    val note = Note(
                        id = noteId,
                        title = noteTitle,
                        description = noteDescription,
                        timestamp = LocalDateTime.now(ZoneId.of("Europe/Warsaw"))
                    )
                    showNoteNotification(note)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete existing channel to update settings
            notificationManager.deleteNotificationChannel(channelId)

            val channel = NotificationChannel(
                channelId,
                "Active Notes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(true)
                setSound(null, null)
                enableLights(true)
                enableVibration(false)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setAllowBubbles(true)
                importance = NotificationManager.IMPORTANCE_HIGH
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNoteNotification(note: Note) {
        try {
            if (!wakeLock.isHeld) {
                wakeLock.acquire(5000L)
            }

            val notificationIntent = packageManager
                .getLaunchIntentForPackage(packageName)
                ?.apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("note_id", note.id)
                }

            val pendingIntent = PendingIntent.getActivity(
                this,
                note.id,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val deleteIntent = PendingIntent.getBroadcast(
                this,
                note.id,
                Intent(this, NotificationDismissedReceiver::class.java).apply {
                    putExtra("note_id", note.id)
                    putExtra("note_title", note.title)
                    putExtra("note_description", note.description)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(note.title)
                .setContentText(note.description)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setTimeoutAfter(0)
                .setDeleteIntent(deleteIntent)
                .build().apply {
                    flags = flags or Notification.FLAG_NO_CLEAR or
                            Notification.FLAG_ONGOING_EVENT or
                            Notification.FLAG_FOREGROUND_SERVICE
                }

            notificationManager.notify(note.id, notification)
            activeNotes.add(note.id)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun stopNotification(noteId: Int) {
        notificationManager.cancel(noteId)
        activeNotes.remove(noteId)

        if (activeNotes.isEmpty()) {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    companion object {
        const val ACTION_STOP_NOTE = "com.sharc.ramdhd.action.STOP_NOTE"
        private const val FOREGROUND_SERVICE_ID = 99999
    }
}