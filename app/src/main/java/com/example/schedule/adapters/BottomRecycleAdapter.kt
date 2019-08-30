package com.example.schedule.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.one_day_layout.view.*
import java.util.*
import android.util.DisplayMetrics
import android.view.Display
import android.content.Context.WINDOW_SERVICE
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat.getSystemService
import android.view.WindowManager
import com.example.schedule.R


class BottomRecycleAdapter(private val allWeekDates: ArrayList<Int>, var context: Context) :
    RecyclerView.Adapter<BottomRecycleAdapter.CustomViewHolder>() {

    var nextWeek = false
    val nameOfWeekdays = arrayOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")
    var currentWeekDates: ArrayList<Int> = arrayListOf(0, 0, 0, 0, 0, 0)
    var itemWidth = context.resources.displayMetrics.widthPixels / 6
    var selectedDay = 4

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


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.itemView.wrapper2.layoutParams.width = itemWidth
        holder.nameOfDayField.text = nameOfWeekdays[position]
        holder.dateField.text = currentWeekDates[position].toString()


    }

    class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nameOfDayField: TextView = itemView.findViewById(com.example.schedule.R.id.nameOfDay)
        val dateField: TextView = itemView.findViewById(com.example.schedule.R.id.date)

        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "$layoutPosition", Toast.LENGTH_SHORT).show()

            }
        }
    }
}