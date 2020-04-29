package com.example.schedule.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.schedule.DownloadController
import com.example.schedule.R
import java.io.File

class SplashScreen : AppCompatActivity() {
    private lateinit var mPreference: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = mPreference.getBoolean(getString(R.string.dark_mode), false)
        if (darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val apk = File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + DownloadController.FILE_NAME
        )
        if (apk.exists()) apk.delete()
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