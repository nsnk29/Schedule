package com.nsnk.schedule_fpm.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.nsnk.schedule_fpm.R

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        with(PreferenceManager.getDefaultSharedPreferences(this)) {
            if (this.getBoolean(getString(R.string.dark_mode), false))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            isFirstRun(this)
        }
    }

    private fun isFirstRun(sp: SharedPreferences) {
        when (sp.getString(
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