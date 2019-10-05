package com.example.schedule.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.schedule.R
import okhttp3.internal.waitMillis

class SplashScreen : AppCompatActivity() {
    private lateinit var mPreference: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = mPreference.getBoolean("dark_mode", false)
        if (darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        isFirstRun()
    }

    private fun isFirstRun() {
        when (mPreference.getString(getString(R.string.savedValueOfUsersPick), "default")){
            "default" -> startActivity(Intent(this, PickerActivity::class.java))
            else -> startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
