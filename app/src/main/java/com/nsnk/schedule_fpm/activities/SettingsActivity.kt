package com.nsnk.schedule_fpm.activities

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.nsnk.schedule_fpm.R
import com.nsnk.schedule_fpm.fragments.SettingsFragment
import com.nsnk.schedule_fpm.receivers.AlertReceiver
import kotlinx.android.synthetic.main.settings_activity.*
import java.util.*


class SettingsActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    var isCountOfLinesChanged: Boolean = false
    private var isGroupChanged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        setSupportActionBar(toolbar as Toolbar?)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        updateFragment()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putBoolean("lines", isCountOfLinesChanged)
            putBoolean("group", isGroupChanged)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        with(savedInstanceState) {
            isCountOfLinesChanged = getBoolean("lines")
            isGroupChanged = getBoolean("group")
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MainActivity.GROUP_PICK_RESULT_CODE) {
            isGroupChanged = true
            updateFragment()
        }
    }


    private fun updateFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, 0)
        startAlarm(c)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
    }

    fun startAlarm(c: Calendar) {
        cancelAlarm()
        val mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val mEditor = mPreference.edit()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val hourString = if (hour < 10) "0$hour" else "$hour"
        val minuteString = if (minute < 10) "0$minute" else "$minute"

        mEditor.putString(getString(R.string.time_notification_picker), "$hourString:$minuteString")
        mEditor.apply()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            c.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Toast.makeText(
            applicationContext,
            "Уведомления установлены на $hourString:$minuteString",
            Toast.LENGTH_LONG
        ).show()
    }

    fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)

        alarmManager.cancel(pendingIntent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        setResults()
        super.onBackPressed()
    }

    private fun setResults() {
        if (isGroupChanged && isCountOfLinesChanged)
            setResult(MainActivity.CODES.BOTH_RESULT_CODE)
        else if (isGroupChanged)
            setResult(MainActivity.CODES.GROUP_PICK_RESULT_CODE)
        else if (isCountOfLinesChanged)
            setResult(MainActivity.CODES.COUNT_LINES_RESULT_CODE)
    }
}

