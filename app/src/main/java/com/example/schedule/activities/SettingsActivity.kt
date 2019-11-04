package com.example.schedule.activities

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
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceChangeListener
import com.example.schedule.AlertReceiver
import com.example.schedule.R
import com.example.schedule.fragments.TimePickerFragment
import kotlinx.android.synthetic.main.settings_activity.*
import java.util.*


class SettingsActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    companion object {
        const val COUNT_LINES_REQUEST_CODE = 1
        const val GROUP_PICK_REQUEST_CODE = 2
        const val BOTH_REQUEST_CODE = 3
    }

    var countOfResults = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        setSupportActionBar(toolbar as Toolbar?)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val mPreference = PreferenceManager.getDefaultSharedPreferences(this.context)


            val notificationSummaryProvider =
                Preference.SummaryProvider<SwitchPreferenceCompat> { preference ->
                    if (preference.isChecked) {
                        "Уведомление придёт в ${mPreference.getString(
                            "time_notification_picker",
                            "18:00"
                        )}"
                    } else {
                        "Уведомления выключены"
                    }
                }


            val notificationStatus =
                findPreference<SwitchPreferenceCompat>(getString(R.string.notification_status))

            notificationStatus?.summaryProvider = notificationSummaryProvider
            notificationStatus!!.onPreferenceChangeListener =
                OnPreferenceChangeListener { _, newValue ->
                    if (!(newValue as Boolean)) {
                        (activity as SettingsActivity).cancelAlarm()
                    } else {
                        val text = mPreference.getString(
                            getString(R.string.time_notification_picker),
                            getString(R.string.default_time_notification)
                        )
                        val slittedText = text?.split(':')?.toTypedArray()
                        val hourOfDay = slittedText?.get(0)?.toInt()
                        val minute = slittedText?.get(1)?.toInt()


                        val c = Calendar.getInstance()
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay!!)
                        c.set(Calendar.MINUTE, minute!!)
                        c.set(Calendar.SECOND, 0)
                        (activity as SettingsActivity).startAlarm(c)
                    }
                    true

                }

            val notificationTimePicker =
                findPreference<Preference>(getString(R.string.notification_time_picker))
            notificationTimePicker?.setOnPreferenceClickListener {
                val timePicker = TimePickerFragment()

                timePicker.show(childFragmentManager, "Время для уведомления")
                true
            }

            val darkMode = findPreference<SwitchPreferenceCompat>(getString(R.string.dark_mode))
            darkMode?.onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                if (!(newValue as Boolean)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                true
            }

            val cardLayoutPreference =
                findPreference<ListPreference>(getString(R.string.card_layout_preference))
            cardLayoutPreference?.onPreferenceChangeListener = OnPreferenceChangeListener { _, _ ->
                (context as SettingsActivity).countOfResults++
                if ((context as SettingsActivity).countOfResults == 1)
                    (context as SettingsActivity).setResult(COUNT_LINES_REQUEST_CODE)
                else (context as SettingsActivity).setResult(BOTH_REQUEST_CODE)
                true
            }

            val sourceSummaryProvider =
                Preference.SummaryProvider<Preference> {
                    "Текущий источник: ${mPreference.getString(
                        getString(R.string.savedValueOfUsersPick),
                        getString(R.string.NA)
                    )}"
                }

            val sourceOfSchedule =
                findPreference<Preference>(getString(R.string.source_of_schedule))
            sourceOfSchedule?.summaryProvider = sourceSummaryProvider

            sourceOfSchedule?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivityForResult(
                    Intent(this.context, PickerActivity::class.java),
                    GROUP_PICK_REQUEST_CODE
                )
                true
            }

        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == GROUP_PICK_REQUEST_CODE) {
                    (context as SettingsActivity).countOfResults++
                    if ((context as SettingsActivity).countOfResults == 1)
                        (context as SettingsActivity).setResult(GROUP_PICK_REQUEST_CODE)
                    else (context as SettingsActivity).setResult(BOTH_REQUEST_CODE)
                    (context as SettingsActivity).updateFragment()

                }
            }
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

    private fun startAlarm(c: Calendar) {
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

    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)

        alarmManager.cancel(pendingIntent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}