package com.example.schedule

import android.Manifest
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.example.schedule.activities.MainActivity
import com.example.schedule.activities.SettingsActivity
import com.google.android.material.snackbar.Snackbar
import java.io.File


class DownloadController(private val activity: AppCompatActivity, private val url: String) {

    companion object {
        const val FILE_NAME = "schedule.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        const val PROVIDER_PATH = ".provider"
        const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
        const val PERMISSION_REQUEST_STORAGE = 0
    }


    fun enqueueDownload() {
        val destination =
            activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/" + FILE_NAME
        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val file = File(destination)
        if (file.exists()) file.delete()
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri).apply {
            setMimeType(MIME_TYPE)
            setTitle(activity.getString(R.string.title_file_download))
            setDescription(activity.getString(R.string.downloading))
            setNotificationVisibility(VISIBILITY_VISIBLE)
            setDestinationUri(uri)
        }

        val id = downloadManager.enqueue(request)
        with(PreferenceManager.getDefaultSharedPreferences(activity).edit()) {
            putString("destination", destination)
            putString("uri", uri.toString())
            putLong("download_id", id)
            commit()
        }
    }

    fun checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            this.enqueueDownload()
        } else {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val layout =
                when (activity) {
                    is SettingsActivity -> activity.findViewById<RelativeLayout>(R.id.mainLayout)
                    is MainActivity -> activity.findViewById<ConstraintLayout>(
                        R.id.wrapper
                    )
                    else -> null
                }
                    ?: return
            Snackbar.make(
                layout,
                R.string.storage_access_required,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_STORAGE
                    )
                }.show()

        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        }
    }
}


private fun getStatus(context: Context, downloadId: Long): Int {
    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val query = DownloadManager.Query()
    query.setFilterById(downloadId)
    val c: Cursor = downloadManager.query(query)
    if (c.moveToFirst()) {
        val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
        c.close()
        return status
    }
    return -1
}

fun isDownloading(
    context: Context,
    downloadId: Long
): Boolean {
    return getStatus(
        context,
        downloadId
    ) == DownloadManager.STATUS_RUNNING
}