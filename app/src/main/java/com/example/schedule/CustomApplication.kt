package com.example.schedule

import android.app.Application
import android.app.DownloadManager
import android.content.IntentFilter
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.receivers.DownloadReceiver

class CustomApplication : Application() {
    lateinit var realm: DatabaseHelper
    override fun onCreate() {
        super.onCreate()
        registerReceiver(DownloadReceiver(), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        realm = DatabaseHelper(this)
    }

    fun getDatabaseInstance(): DatabaseHelper {
        if (!this::realm.isInitialized)
            realm = DatabaseHelper(this)
        return realm
    }
}