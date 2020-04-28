package com.example.schedule

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import java.io.File


class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
        val destination: String
        val uri: Uri
        val savedDownloadID: Long
        with(mPreference) {
            destination = getString("destination", "") ?: ""
            uri = Uri.parse(getString("uri", ""))
            savedDownloadID = getLong("download_id", 0L)
        }
        if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) != savedDownloadID)
            return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val install = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                data = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + DownloadController.PROVIDER_PATH,
                    File(destination)
                )
            }
            context.startActivity(install)
        } else {
            val install = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                setDataAndType(
                    uri,
                    DownloadController.APP_INSTALL_PATH
                )
            }
            context.startActivity(install)
        }
    }
}