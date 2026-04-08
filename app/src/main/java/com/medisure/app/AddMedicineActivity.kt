package com.medisure.app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMedicineActivity : AppCompatActivity() {

    private var selectedHour = -1
    private var selectedMinute = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medicine)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val btnSelectTime = findViewById<Button>(R.id.btnSelectTime)
        val textSelectedTime = findViewById<android.widget.TextView>(R.id.textSelectedTime)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSelectTime.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)

            android.app.TimePickerDialog(this, R.style.MediSure_TimePicker, { _, h, m ->
                selectedHour = h
                selectedMinute = m
                val timeFormat = String.format("%02d:%02d", h, m)
                textSelectedTime.text = timeFormat
            }, hour, minute, true).show()
        }
        
        btnSave.setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.editMedicineName).text.toString()
            val dosage = findViewById<TextInputEditText>(R.id.editDosage).text.toString()
            val stockStr = findViewById<TextInputEditText>(R.id.editStock).text.toString()
            val instructions = findViewById<TextInputEditText>(R.id.editInstructions).text.toString()
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Medicine name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedHour == -1) {
                Toast.makeText(this, "Please select a reminder time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val userId = SessionManager.getUserId(this)
            
            if (userId.isNullOrEmpty()) {
                Toast.makeText(this, "Session expired. Please Login again.", Toast.LENGTH_LONG).show()
                finish()
                return@setOnClickListener
            }

            val stock = stockStr.toIntOrNull() ?: 0
            val reminderTime = String.format("%02d:%02d", selectedHour, selectedMinute)

            // Check Alarm Permissions explicitly for Android 12+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(android.app.AlarmManager::class.java)
                if (!alarmManager.canScheduleExactAlarms()) {
                     val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                     startActivity(intent)
                     Toast.makeText(this, "Please allow Exact Alarms to continue", Toast.LENGTH_LONG).show()
                     return@setOnClickListener
                }
            }

            // Schedule Alarm (pass 0 as context flag if needed, usually passed implicitly)
            scheduleNotification(name, dosage)
            
            val request = MedicineRequest(
                user_id = userId,
                name = name,
                dosage = dosage,
                stock = stock,
                instructions = instructions,
                reminder_time = reminderTime
            )
            
            NetworkClient.getApi(this).addMedicine(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful || response.code() == 201) {
                        Toast.makeText(this@AddMedicineActivity, "Medicine & Reminder Saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                    if (response.isSuccessful || response.code() == 201) {
                        Toast.makeText(this@AddMedicineActivity, "Medicine & Reminder Saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorStr = response.errorBody()?.string() ?: ""
                        // Simple regex or substring to find meaningful message to avoid crash with full JSON parser overhead if not needed
                        // JSON usually: {"code":"...", "details":"...", "hint":"...", "message":"..."}
                        val details = if (errorStr.contains("details")) {
                            errorStr.substringAfter("details\":\"").substringBefore("\"")
                        } else if (errorStr.contains("message")) {
                            errorStr.substringAfter("message\":\"").substringBefore("\"")
                        } else {
                            errorStr
                        }
                        
                        val cleanError = if (details.length > 50) details.take(50) + "..." else details
                        Toast.makeText(this@AddMedicineActivity, "Error ${response.code()}: $cleanError", Toast.LENGTH_LONG).show()
                    }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                     Toast.makeText(this@AddMedicineActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun scheduleNotification(medicineName: String, dosage: String) {
        val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(this, ReminderReceiver::class.java).apply {
            putExtra("medicine_name", medicineName)
            putExtra("medicine_dosage", dosage)
            putExtra("medicine_id", medicineName)
        }
        
        // Use unique ID for PendingIntent to avoid overwriting (simplified for demo using hashCode)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this, 
            medicineName.hashCode(), 
            intent, 
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
            set(java.util.Calendar.MINUTE, selectedMinute)
            set(java.util.Calendar.SECOND, 0)
        }
        
        // If time is in past, add 1 day
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                     alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                     Toast.makeText(this, "Please allow Exact Alarms in Settings", Toast.LENGTH_LONG).show()
                     // Ideally redirect to settings here
                }
            } else {
                 alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission required for alarms", Toast.LENGTH_SHORT).show()
        }
    }
}
