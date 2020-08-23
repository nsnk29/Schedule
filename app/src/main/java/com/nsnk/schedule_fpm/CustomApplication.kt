package com.nsnk.schedule_fpm

import android.app.Application
import com.nsnk.schedule_fpm.database.DatabaseHelper

class CustomApplication : Application() {
    lateinit var realm: DatabaseHelper
    override fun onCreate() {
        super.onCreate()
        realm = DatabaseHelper(this)
    }

    fun getDatabaseInstance(): DatabaseHelper {
        if (!this::realm.isInitialized)
            realm = DatabaseHelper(this)
        return realm
    }
}