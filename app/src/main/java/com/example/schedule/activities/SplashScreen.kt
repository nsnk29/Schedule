package com.example.schedule.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.schedule.R

class SplashScreen : AppCompatActivity() {
    private lateinit var mPreference: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = mPreference.getBoolean(getString(R.string.dark_mode), false)
        if (darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        isFirstRun()
    }

    private fun isFirstRun() {
        when (mPreference.getString(
            getString(R.string.savedValueOfUsersPick),
            getString(R.string.default_string)
        )) {
            getString(R.string.default_string) -> startActivity(
                Intent(
                    this,
                    PickerActivity::class.java
                )
            )
            else -> startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
