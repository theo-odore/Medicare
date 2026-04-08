package com.medicare.app

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MedicineStatusManager {
    private const val PREF_NAME = "MedicineStatusPrefs"

    private fun getPrefs(context: Context) = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun getTodayKey(medicineId: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "taken_${medicineId}_$date"
    }

    fun markAsTaken(context: Context, medicineId: String) {
        getPrefs(context).edit().putBoolean(getTodayKey(medicineId), true).apply()
    }

    fun isTaken(context: Context, medicineId: String): Boolean {
        return getPrefs(context).getBoolean(getTodayKey(medicineId), false)
    }
    
    fun getTakenCountForToday(context: Context, medicineIds: List<String>): Int {
        return medicineIds.count { isTaken(context, it) }
    }
}
