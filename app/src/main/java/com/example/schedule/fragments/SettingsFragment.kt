package com.example.schedule.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.example.schedule.R
import com.example.schedule.activities.MainActivity
import com.example.schedule.activities.PickerActivity
import com.example.schedule.activities.SettingsActivity
import java.util.*

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
        if (notificationStatus != null) {
            notificationStatus.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
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
        }

        val notificationTimePicker =
            findPreference<Preference>(getString(R.string.notification_time_picker))
        notificationTimePicker?.setOnPreferenceClickListener {
            val timePicker = TimePickerFragment()

            timePicker.show(childFragmentManager, "Время для уведомления")
            true
        }

        val darkMode = findPreference<SwitchPreferenceCompat>(getString(R.string.dark_mode))
        darkMode?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (!(newValue as Boolean)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                true
            }

        val cardLayoutPreference =
            findPreference<ListPreference>(getString(R.string.card_layout_preference))
        cardLayoutPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                (context as SettingsActivity).countOfResults++
                if ((context as SettingsActivity).countOfResults == 1)
                    (context as SettingsActivity).setResult(MainActivity.CODES.COUNT_LINES_REQUEST_CODE)
                else (context as SettingsActivity).setResult(MainActivity.CODES.BOTH_REQUEST_CODE)
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
                MainActivity.CODES.GROUP_PICK_REQUEST_CODE
            )
            true
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MainActivity.CODES.GROUP_PICK_REQUEST_CODE) {
                (context as SettingsActivity).countOfResults++
                if ((context as SettingsActivity).countOfResults == 1)
                    (context as SettingsActivity).setResult(MainActivity.CODES.GROUP_PICK_REQUEST_CODE)
                else (context as SettingsActivity).setResult(MainActivity.CODES.BOTH_REQUEST_CODE)
                (context as SettingsActivity).updateFragment()
            }
        }
    }
}