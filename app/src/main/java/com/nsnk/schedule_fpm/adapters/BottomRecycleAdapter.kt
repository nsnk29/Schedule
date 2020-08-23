package com.nsnk.schedule_fpm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nsnk.schedule_fpm.R
import com.nsnk.schedule_fpm.interfaces.BottomRecyclerClickListener
import kotlinx.android.synthetic.main.day_item_layout.view.*


class BottomRecycleAdapter(
    val allWeekDates: List<Int>, var context: Context,
    val currentDay: Int, private var isSecondWeek: Boolean,
    private val itemClickListener: BottomRecyclerClickListener
) :
    RecyclerView.Adapter<BottomRecycleAdapter.WeekDayViewHolder>() {


    private val nameOfWeekdays = arrayOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")
    private var currentWeekDates: Array<Int> = Array(6) { 0 }
    private var itemWidth = context.resources.displayMetrics.widthPixels / 6
    var selectedDay = currentDay
    private var selectedTextColor: Int
    private var primaryTextColor: Int


    init {
        initWeekDate()
        selectedTextColor = ContextCompat.getColor(
            context,
            R.color.selected_text_color
        )
        primaryTextColor = ContextCompat.getColor(
            context,
            R.color.text_color_primary
        )
    }


    private fun initWeekDate() {
        if (!isSecondWeek)
            for (i in 0..5) currentWeekDates[i] = allWeekDates[i]
        else
            for (i in 7..12) currentWeekDates[i - 7] = allWeekDates[i]
    }

    fun changeCurrentWeekDate() {
        isSecondWeek = !isSecondWeek
        initWeekDate()
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return 6
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekDayViewHolder =
        WeekDayViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.day_item_layout, parent, false),
            itemWidth
        )


    override fun onBindViewHolder(holder: WeekDayViewHolder, position: Int) {
        holder.bind(
            currentWeekDates[position].toString(),
            nameOfWeekdays[position],
            itemClickListener
        )

        when {
            position == selectedDay -> {
                holder.setBackgroundAndTextColor(R.drawable.rounded_frame_filled, selectedTextColor)
            }
            (position == currentDay) and (!isSecondWeek) -> {
                holder.setBackgroundAndTextColor(R.drawable.rounded_frame, primaryTextColor)
            }
            else -> {
                holder.setBackgroundAndTextColor(0, primaryTextColor)
            }
        }

    }

    class WeekDayViewHolder(v: View, itemWidth: Int) : RecyclerView.ViewHolder(v) {
        private val nameOfDayTextView: TextView = itemView.nameOfDay
        private val dateTextView: TextView = itemView.dateTextView

        init {
            itemView.wrapper.layoutParams.width = itemWidth
        }

        fun bind(dayName: String, date: String, itemClickListener: BottomRecyclerClickListener) {
            nameOfDayTextView.text = dayName
            dateTextView.text = date
            itemView.setOnClickListener { itemClickListener.onItemClicked(layoutPosition) }
        }

        fun setBackgroundAndTextColor(background: Int, textColor: Int) {
            itemView.nameOfDay.setBackgroundResource(background)
            itemView.nameOfDay.setTextColor(textColor)
        }
    }
}

