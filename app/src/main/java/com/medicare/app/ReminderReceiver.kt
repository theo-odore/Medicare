package com.medicare.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val dosage = intent.getStringExtra("medicine_dosage") ?: ""
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medicare_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Medicine Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Action Intents (Stubbed for now - would normally call API to log status)
        val takenIntent = Intent(context, MainActivity::class.java) // Just open app for now
        val takenPendingIntent = PendingIntent.getActivity(context, 101, takenIntent, PendingIntent.FLAG_IMMUTABLE)
        
        val missedIntent = Intent(context, MainActivity::class.java)
        val missedPendingIntent = PendingIntent.getActivity(context, 102, missedIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_lock) // Using generic lock icon as placeholder for bell
            .setContentTitle("Medicine Reminder")
            .setContentText("Time to take $medicineName ($dosage)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_person, "Taken", takenPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Missed", missedPendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
