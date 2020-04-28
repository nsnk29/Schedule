package com.example.schedule

import android.app.Application
import android.app.DownloadManager
import android.content.IntentFilter

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerReceiver(DownloadReceiver(), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}