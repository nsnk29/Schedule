package com.example.schedule.model

import android.content.Context
import com.example.schedule.R
import java.util.*
import kotlin.collections.ArrayList

object CalendarHelper {
    var currentDay: Int = 0
    var parity: Int = 0

    init {
        updateCurrentInfo()
    }

    fun updateCurrentInfo(): Boolean {
        if (currentDay != getDayOfWeek() || parity != getParityOfWeek()) {
            currentDay = getDayOfWeek()
            parity = getParityOfWeek()
            return true
        }
        return false
    }

    private fun getInstanceOfCurrentDay(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.apply {
            firstDayOfWeek = GregorianCalendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
        }
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1)
            calendar.add(Calendar.DATE, 1)
        return calendar
    }


    private fun getDayOfWeek(): Int {
        val day = getInstanceOfCurrentDay().get(Calendar.DAY_OF_WEEK) - 2
        return if (day == 0) 0 else day
    }

    fun initAllWeekDates(): ArrayList<Int> {
        val allWeekDates: ArrayList<Int> = ArrayList(14)
        val cal = getInstanceOfCurrentDay().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }
        for (i in 0 until 14) {
            allWeekDates.add(cal.get(Calendar.DAY_OF_MONTH))
            cal.add(Calendar.DATE, 1)
        }
        return allWeekDates
    }

    // ToDo при переделке бэка сайта нужно будет получать 0/1 значение в JSON и решать что возвращать
    private fun getParityOfWeek(): Int = getInstanceOfCurrentDay().get(Calendar.WEEK_OF_YEAR) % 2


    fun getDayName(context: Context, pos: Int) =
        when (pos) {
            0 -> (context.getString(R.string.monday))
            1 -> (context.getString(R.string.tuesday))
            2 -> (context.getString(R.string.wednesday))
            3 -> (context.getString(R.string.thursday))
            4 -> (context.getString(R.string.friday))
            5 -> (context.getString(R.string.saturday))
            else -> (context.getString(R.string.sunday))
        }

    fun getNegativeWeek(x: Int) = when (x) {
        1 -> 0
        else -> 1
    }
}