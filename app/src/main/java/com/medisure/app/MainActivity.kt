package com.medisure.app

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {

    private lateinit var rvMedicines: RecyclerView
    private lateinit var textAdherence: TextView
    private lateinit var textTakenCount: TextView
    private lateinit var textPendingCount: TextView
    private lateinit var textMissedCount: TextView
    private lateinit var textProgressPercent: TextView
    private lateinit var textProgressFraction: TextView
    private lateinit var progressBarDaily: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        // Check Session
        val userId = SessionManager.getUserId(this)
        if (userId.isNullOrEmpty()) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        setContentView(R.layout.activity_main)
        
        // Setup Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Init Views
        rvMedicines = findViewById(R.id.rvMedicines)
        rvMedicines.layoutManager = LinearLayoutManager(this)
        
        textAdherence = findViewById(R.id.textAdherence)
        textTakenCount = findViewById(R.id.textTakenCount)
        textPendingCount = findViewById(R.id.textPendingCount)
        textMissedCount = findViewById(R.id.textMissedCount)
        textProgressPercent = findViewById(R.id.textProgressPercent)
        textProgressFraction = findViewById(R.id.textProgressFraction)
        progressBarDaily = findViewById(R.id.progressBarDaily)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
             val intent = Intent(this, AddMedicineActivity::class.java)
             startActivity(intent)
        }
        
        fetchMedicines(userId)
        
        createNotificationChannel()
        checkNotificationPermission()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Medicine Reminders"
            val descriptionText = "Notifications for medicine reminders"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("medisure_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from AddMedicineActivity
        val userId = SessionManager.getUserId(this)
        if (!userId.isNullOrEmpty()) {
            fetchMedicines(userId)
        }
    }

    private fun fetchMedicines(userId: String) {
        val userIdQuery = "eq.$userId"
        NetworkClient.getApi(this).getMedicines(userIdQuery).enqueue(object : Callback<List<MedicineResponse>> {
            override fun onResponse(call: Call<List<MedicineResponse>>, response: Response<List<MedicineResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val medicines = response.body()!!
                    setupDashboard(medicines)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load medicines", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MedicineResponse>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDashboard(medicines: List<MedicineResponse>) {
        val adapter = MedicineAdapter(medicines.toMutableList()) {
            updateStats(medicines.size) // Note: this total count won't update dynamically on delete without more refactoring, but OK for now.
        }
        rvMedicines.adapter = adapter
        
        // Swipe to Delete
        val swipeCallback = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val medicine = adapter.getItem(position)
                
                // Optimistically remove from UI
                adapter.removeAt(position)
                
                // Call API to delete
                val idQuery = "eq.${medicine.id}"
                NetworkClient.getApi(this@MainActivity).deleteMedicine(idQuery).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                       if (response.isSuccessful || response.code() == 204) {
                           Toast.makeText(this@MainActivity, "Medicine removed", Toast.LENGTH_SHORT).show()
                       } else {
                           Toast.makeText(this@MainActivity, "Failed to delete: ${response.code()}", Toast.LENGTH_SHORT).show()
                           // Ideally restore item here
                       }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                         Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = android.graphics.drawable.ColorDrawable(android.graphics.Color.RED)
                
                // Draw Red Background
                if (dX < 0) { // Swiping Left
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)
                
                // Draw Delete Icon
                val deleteIcon = androidx.core.content.ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete_white)
                if (deleteIcon != null && dX < -150) { // Only show icon if swiped enough
                    val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight
                    val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(rvMedicines)
        
        // Initial Stats Update
        updateStats(medicines.size)
    }
    
    private fun updateStats(total: Int) {
        val adapter = rvMedicines.adapter as? MedicineAdapter ?: return
        val taken = adapter.getTakenCount()
        val pending = total - taken
        
        val adherence = if (total > 0) (taken * 100 / total) else 0

        // Update UI
        textAdherence.text = "$adherence%"
        textTakenCount.text = taken.toString()
        textPendingCount.text = pending.toString()
        // Missed count logic to be added later
        
        textProgressPercent.text = "$adherence%"
        textProgressFraction.text = "$taken of $total medicines taken"
        
        progressBarDaily.max = total
        progressBarDaily.progress = taken
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_logout -> {
                SessionManager.clearSession(this)
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
