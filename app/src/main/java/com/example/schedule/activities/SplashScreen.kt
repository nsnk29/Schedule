package com.example.schedule.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.schedule.R
import okhttp3.internal.waitMillis

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = mPreference.getBoolean("dark_mode", false)
        if (darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        isFirstRun()
        finish()
    }

    private fun isFirstRun() {
//        startActivity(Intent(this, MainActivity::class.java))
        startActivity(Intent(this, PickerActivity::class.java))
    }
}
