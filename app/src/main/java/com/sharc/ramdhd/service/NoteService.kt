package com.sharc.ramdhd.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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
    private val activeNotes = mutableSetOf<Int>()  // Track active note IDs

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

        // Start the service with an empty notification to keep it running
        startForeground(FOREGROUND_SERVICE_ID, createBaseNotification())
    }

    private fun createBaseNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("You still have some notes remaining")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .build()
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
            val channel = NotificationChannel(
                channelId,
                "Active Notes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(true)
                setSound(null, null)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNoteNotification(note: Note) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10*60*1000L)
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

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(note.title)
            .setContentText(note.description)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()

        // Instead of using startForeground, use notify
        notificationManager.notify(note.id, notification)
        activeNotes.add(note.id)
    }

    private fun stopNotification(noteId: Int) {
        notificationManager.cancel(noteId)
        activeNotes.remove(noteId)

        // If no more active notes, stop the service
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
        private const val FOREGROUND_SERVICE_ID = 99999  // Unique ID for the service notification
    }
}