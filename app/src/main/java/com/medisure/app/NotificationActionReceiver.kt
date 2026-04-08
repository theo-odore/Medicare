package com.medisure.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medicineId = intent.getStringExtra("medicine_id")
        val notificationId = intent.getIntExtra("notification_id", 0)

        if (action == "ACTION_TAKEN" && !medicineId.isNullOrEmpty()) {
            // Mark as taken in shared prefs
            MedicineStatusManager.markAsTaken(context, medicineId)
            
            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            
            // Optional: Show a subtle toast? Or explicit silence? 
            // User asked it "should not open app". System might show toast.
        }
    }
}
