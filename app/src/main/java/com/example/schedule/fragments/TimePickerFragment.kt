package com.example.schedule.fragments


import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment

import java.util.Calendar

class TimePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 1)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)


        return TimePickerDialog(
            activity, activity as TimePickerDialog.OnTimeSetListener?, hour, minute, DateFormat.is24HourFormat(
                activity
            )
        )
    }
}