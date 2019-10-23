package com.example.schedule.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.activities.MainActivity
import kotlinx.android.synthetic.main.one_day_layout.view.*
import java.util.*


class BottomRecycleAdapter(
    private val allWeekDates: ArrayList<Int>, var context: Context,
    val currentDay: Int
) :
    RecyclerView.Adapter<BottomRecycleAdapter.CustomViewHolder>() {


    val nameOfWeekdays = arrayOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")
    var currentWeekDates: ArrayList<Int> = arrayListOf(0, 0, 0, 0, 0, 0)
    var itemWidth = context.resources.displayMetrics.widthPixels / 6
    var selectedDay = currentDay
    var nextWeek: Boolean = false


    init {
        initWeekDate()
    }


    private fun initWeekDate() {
        if (!nextWeek)
            for (i in 0..5) currentWeekDates[i] = allWeekDates[i]
        else
            for (i in 7..12) currentWeekDates[i - 7] = allWeekDates[i]
    }

    fun changeCurrentWeekDate() {
        nextWeek = !nextWeek
        if (!nextWeek)
            for (i in 0..5) currentWeekDates[i] = allWeekDates[i]
        else
            for (i in 7..12) currentWeekDates[i - 7] = allWeekDates[i]

    }


    override fun getItemCount(): Int {
        return 6
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.one_day_layout, parent, false)

        return CustomViewHolder(view)
    }


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.itemView.wrapper.layoutParams.width = itemWidth
        holder.nameOfDayField.text = currentWeekDates[position].toString()
        holder.dateField.text = nameOfWeekdays[position]

        when {
            position == selectedDay -> {
                holder.itemView.nameOfDay.setBackgroundResource(R.drawable.rounded_frame_filled)
                holder.itemView.nameOfDay.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.selected_text_color
                    )
                )
            }
            (position == currentDay) and (!nextWeek) -> {
                holder.itemView.nameOfDay.setBackgroundResource(R.drawable.rounded_frame)
                holder.itemView.nameOfDay.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.text_color_primary
                    )
                )
            }
            else -> {
                holder.itemView.nameOfDay.setBackgroundResource(0)
                holder.itemView.nameOfDay.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.text_color_primary
                    )
                )
            }
        }

    }

    class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nameOfDayField: TextView = itemView.findViewById(R.id.nameOfDay)
        val dateField: TextView = itemView.findViewById(R.id.dateTextView)

        init {
            itemView.setOnClickListener {
                (itemView.context as MainActivity).updateBottomRecycler(layoutPosition, false)
            }
        }
    }
}