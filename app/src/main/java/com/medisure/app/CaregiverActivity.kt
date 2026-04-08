package com.medisure.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class CaregiverActivity : AppCompatActivity() {

    private lateinit var rvPatients: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caregiver)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        rvPatients = findViewById(R.id.rvPatients)
        rvPatients.layoutManager = LinearLayoutManager(this)

        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fabAddPatient)
        fab.setOnClickListener {
            Toast.makeText(this, "Add Patient feature coming soon", Toast.LENGTH_SHORT).show()
        }

        loadMockPatients()
    }

    private fun loadMockPatients() {
        val patients = listOf(
            Patient(
                id = "1",
                name = "John Doe",
                age = 72,
                lastActive = "2 mins ago",
                riskLevel = RiskLevel.LOW,
                adherence = 85,
                alerts = 0
            ),
            Patient(
                id = "2",
                name = "Mary Smith",
                age = 68,
                lastActive = "1 hour ago",
                riskLevel = RiskLevel.HIGH,
                adherence = 45,
                alerts = 3
            ),
            Patient(
                id = "3",
                name = "Robert Johnson",
                age = 75,
                lastActive = "30 mins ago",
                riskLevel = RiskLevel.MEDIUM,
                adherence = 72,
                alerts = 1
            )
        )

        rvPatients.adapter = PatientAdapter(patients)
    }
}

data class Patient(
    val id: String,
    val name: String,
    val age: Int,
    val lastActive: String,
    val riskLevel: RiskLevel,
    val adherence: Int,
    val alerts: Int
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}
