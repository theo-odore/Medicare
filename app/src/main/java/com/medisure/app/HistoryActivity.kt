package com.medisure.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        val userId = SessionManager.getUserId(this)
        if (userId != null) {
            fetchHistory(userId)
        }
    }

    private fun fetchHistory(userId: String) {
        val userIdQuery = "eq.$userId"
        NetworkClient.getApi(this).getMedicines(userIdQuery).enqueue(object : Callback<List<MedicineResponse>> {
            override fun onResponse(call: Call<List<MedicineResponse>>, response: Response<List<MedicineResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    // For demo purposes, pretend all synced fetched medicines are history
                    // Ideally we would filter or fetch from a 'history' table
                    val medicines = response.body()!!
                    setupHistoryList(medicines)
                } else {
                    Toast.makeText(this@HistoryActivity, "Failed to load history", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MedicineResponse>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupHistoryList(medicines: List<MedicineResponse>) {
        val adapter = MedicineAdapter(medicines.toMutableList()) {
            // Nothing to update on history click for now
        }
        rvHistory.adapter = adapter
    }
}
