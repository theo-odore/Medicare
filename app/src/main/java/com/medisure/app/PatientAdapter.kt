package com.medisure.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PatientAdapter(private val patients: List<Patient>) :
    RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textName)
        val textDetails: TextView = view.findViewById(R.id.textDetails)
        val textRisk: TextView = view.findViewById(R.id.textRisk)
        val textAdherence: TextView = view.findViewById(R.id.textAdherence)
        val textAlerts: TextView = view.findViewById(R.id.textAlerts)
        val btnViewDetails: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnViewDetails)
        val btnCall: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]
        holder.textName.text = patient.name
        holder.textDetails.text = "Age: ${patient.age} • Last active: ${patient.lastActive}"
        
        holder.textAdherence.text = "Adherence: ${patient.adherence}%"
        
        // Risk Badge Config
        when (patient.riskLevel) {
            RiskLevel.LOW -> {
                holder.textRisk.text = "● LOW RISK"
                holder.textRisk.setTextColor(android.graphics.Color.parseColor("#388E3C"))
                holder.textRisk.setBackgroundResource(R.drawable.bg_badge_low_risk)
            }
            RiskLevel.MEDIUM -> {
                holder.textRisk.text = "● MEDIUM RISK"
                holder.textRisk.setTextColor(android.graphics.Color.parseColor("#F57C00"))
                holder.textRisk.setBackgroundResource(R.drawable.bg_badge_pending) // Reusing pending/orange bg
            }
            RiskLevel.HIGH -> {
                holder.textRisk.text = "● HIGH RISK"
                holder.textRisk.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                holder.textRisk.setBackgroundResource(R.drawable.bg_badge_high_risk)
            }
        }

        // Alerts Badge
        if (patient.alerts > 0) {
            holder.textAlerts.visibility = View.VISIBLE
            holder.textAlerts.text = "${patient.alerts} Alerts"
        } else {
            holder.textAlerts.visibility = View.GONE
        }
        
        holder.btnCall.setOnClickListener { 
            // Mock Call Intent
        }
        
        holder.btnViewDetails.setOnClickListener {
            // Mock Details intent
        }
    }

    override fun getItemCount() = patients.size
}
