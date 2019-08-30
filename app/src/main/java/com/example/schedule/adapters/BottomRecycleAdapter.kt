package com.example.schedule.adapters

import android.app.Activity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import android.util.TypedValue
import kotlinx.android.synthetic.main.one_day_layout.view.*


class BottomRecycleAdapter(private val allWeekDates: ArrayList<Int>) :
    RecyclerView.Adapter<BottomRecycleAdapter.CustomViewHolder>() {

    var nextWeek = false
    val nameOfWeekdays = arrayOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")
    var currentWeekDates: ArrayList<Int> = arrayListOf(0, 0, 0, 0, 0, 0)

    init {
        changeCurrentWeekDate()
    }



    fun changeCurrentWeekDate(){
        if (!nextWeek) {
            for (i in 0..5) currentWeekDates[i] = allWeekDates[i]
            nextWeek = !nextWeek
        } else {
            for (i in 7..12) currentWeekDates[i-7] = allWeekDates[i]
            nextWeek = !nextWeek
        }
    }


    override fun getItemCount(): Int {
        return 6
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(com.example.schedule.R.layout.one_day_layout, parent, false)

        return CustomViewHolder(view)
    }


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
//        holder.itemView.layoutParams.width = 178
//        println("MY TAG ${holder.itemView.nameOfDay.layoutParams.width}")
//        holder.itemView.nameOfDay.layoutParams.width
//        holder.itemView.nameOfDay.layoutParams.height = 469
        holder.itemView.wrapper2.layoutParams.width = 178


        holder.nameOfDayField.text = nameOfWeekdays[position]
        holder.dateField.text = currentWeekDates[position].toString()

    }

    class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nameOfDayField: TextView = itemView.findViewById(com.example.schedule.R.id.nameOfDay)
        val dateField: TextView = itemView.findViewById(com.example.schedule.R.id.date)

        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "$layoutPosition", Toast.LENGTH_LONG).show()
            }
        }
    }
}