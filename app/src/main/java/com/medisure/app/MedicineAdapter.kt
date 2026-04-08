package com.medisure.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicineAdapter(
    private val medicines: MutableList<MedicineResponse>, 
    private val onStatusChange: () -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {
    
    // Local set to track taken medicines for this session (since DB logic isn't fully ready)
    private val takenIds = mutableSetOf<String>()

    class MedicineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textName)
        val textDosage: TextView = view.findViewById(R.id.textDosage)
        val textTime: TextView = view.findViewById(R.id.textTime)
        val textInstructions: TextView = view.findViewById(R.id.textInstructions)
        val textStatusBadge: TextView = view.findViewById(R.id.textStatusBadge)
        val layoutActions: View = view.findViewById(R.id.layoutActions)
        val btnTaken: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnTaken)
        val btnMissed: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnMissed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.textName.text = medicine.name
        holder.textDosage.text = medicine.dosage
        holder.textInstructions.text = medicine.instructions
        holder.textTime.text = medicine.reminder_time ?: "No Time"

        val isTaken = takenIds.contains(medicine.id)

        if (isTaken) {
            holder.textStatusBadge.text = "Taken"
            holder.textStatusBadge.setTextColor(android.graphics.Color.parseColor("#388E3C")) // Green
            holder.textStatusBadge.setBackgroundResource(R.drawable.bg_badge_taken)
            holder.layoutActions.visibility = View.GONE
        } else {
            holder.textStatusBadge.text = "Pending"
            holder.textStatusBadge.setTextColor(android.graphics.Color.parseColor("#F57C00")) // Orange
            holder.textStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
            holder.layoutActions.visibility = View.VISIBLE
        }
        
        holder.btnTaken.setOnClickListener {
            takenIds.add(medicine.id)
            notifyItemChanged(position)
            onStatusChange() // Callback to update Main Activity Stats
        }
        
        holder.btnMissed.setOnClickListener {
            // Logic for missed could be similar, or just hide row. For now, just toast or no-op visually
            holder.layoutActions.visibility = View.GONE
            holder.textStatusBadge.text = "Missed"
            holder.textStatusBadge.setTextColor(android.graphics.Color.RED)
        }
    }

    override fun getItemCount() = medicines.size
    
    fun getTakenCount(): Int = takenIds.size

    fun getItem(position: Int): MedicineResponse = medicines[position]

    fun removeAt(position: Int) {
        if (position in medicines.indices) {
            medicines.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, medicines.size)
        }
    }
}
