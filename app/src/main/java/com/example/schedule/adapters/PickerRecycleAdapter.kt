package com.example.schedule.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R

class PickerRecycleAdapter(var context: Context) : RecyclerView.Adapter<PickerRecycleAdapter.CustomViewHolder3>() {


    override fun onBindViewHolder(holder: CustomViewHolder3, position: Int) {
        holder.dateField.text = "ыы"
        Toast.makeText(context, "NIce", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder3 {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.one_day_layout, parent, false)
        return CustomViewHolder3(view)

    }

    override fun getItemCount(): Int {
        return 5
    }


    class CustomViewHolder3(v: View) : RecyclerView.ViewHolder(v) {
        val nameOfDayField: TextView = itemView.findViewById(R.id.nameOfDay)
        val dateField: TextView = itemView.findViewById(R.id.dateTextView)

    }

}

