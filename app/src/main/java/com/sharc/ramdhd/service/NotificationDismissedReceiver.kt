package com.sharc.ramdhd.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper

class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Re-show notification after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            val noteId = intent.getIntExtra("note_id", -1)
            val serviceIntent = Intent(context, NoteService::class.java).apply {
                putExtra("note_id", noteId)
                putExtra("note_title", intent.getStringExtra("note_title"))
                putExtra("note_description", intent.getStringExtra("note_description"))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }, 1000) // 1 second delay
    }
}