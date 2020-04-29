package com.example.schedule.fragments

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.example.schedule.R
import com.example.schedule.URLRequests
import com.example.schedule.activities.MainActivity
import com.example.schedule.activities.PickerActivity
import com.example.schedule.activities.SettingsActivity
import com.example.schedule.isDownloading
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.settings_activity.*
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
                if (!(newValue as Boolean))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                true
            }

        val cardLayoutPreference =
            findPreference<ListPreference>(getString(R.string.card_layout_preference))
        cardLayoutPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                if (activity is SettingsActivity)
                    (activity as SettingsActivity).isCountOfLinesChanged = true
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
            activity?.startActivityForResult(
                Intent(this.context, PickerActivity::class.java),
                MainActivity.CODES.GROUP_PICK_RESULT_CODE
            )
            true
        }

        val updatePreference = findPreference<Preference>(getString(R.string.update_app))
        updatePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val id = mPreference.getLong("download_id", 0L)
            if (activity != null && !isDownloading(requireActivity(), id))
                URLRequests.checkUpdate(activity as AppCompatActivity)
            else
                Snackbar.make(
                    (activity as SettingsActivity).mainLayout,
                    R.string.update_in_process,
                    Snackbar.LENGTH_LONG
                ).show()
            true
        }

        val aboutPreference = findPreference<Preference>(getString(R.string.about_app))
        aboutPreference?.setOnPreferenceClickListener {
            Toast.makeText(context, "Soon", Toast.LENGTH_SHORT).show()
            true
        }
    }
}