package com.example.schedule.model

import android.content.Context
import com.example.schedule.R
import java.util.*
import kotlin.collections.ArrayList

fun initAllWeekDates(): ArrayList<Int> {
    val allWeekDates: ArrayList<Int> = ArrayList(14)

    val cal = Calendar.getInstance()
    if (cal.get(Calendar.DAY_OF_WEEK) == 1)
        cal.add(Calendar.DATE, 1)
    cal.apply {
        firstDayOfWeek = GregorianCalendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        clear(Calendar.MINUTE)
        clear(Calendar.SECOND)
        clear(Calendar.MILLISECOND)
        set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    }

    for (i in 0 until 14) {
        allWeekDates.add(cal.get(Calendar.DAY_OF_MONTH))
        cal.add(Calendar.DATE, 1)
    }
    return allWeekDates
}


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

fun getCurrentDay(): Int {
    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
    return if (day == -1) 0 else day
}

fun getParityOfWeek(): Int {
    val cal = Calendar.getInstance()
    if (cal.get(Calendar.DAY_OF_WEEK) == 1)
        cal.add(Calendar.DATE, 1)
    return cal.get(Calendar.WEEK_OF_YEAR) % 2
}

