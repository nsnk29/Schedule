package com.example.schedule

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.schedule.activities.MainActivity
import com.example.schedule.activities.PickerActivity
import com.example.schedule.activities.SettingsActivity
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.fragments.UpdateDialogFragment
import com.example.schedule.model.LessonJSONStructure
import com.example.schedule.model.UpdateJSONScheme
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.settings_activity.*
import okhttp3.*
import java.io.IOException
import java.net.URL


object URLRequests {

    var lastDownloadController: DownloadController? = null

    fun getLessonsJSON(context: Context, isUpdate: Boolean = false) {
        fun hideLoading(isUpdate: Boolean, context: Context) {
            if (isUpdate && context is PickerActivity) {
                context.hideLoading()
            }
        }

        val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
        val client = OkHttpClient()
        val url = URL(
            context.getString(R.string.URL_LESSONS_JSON).replace(
                "VERSION",
                mPreference.getLong(context.getString(R.string.version), 0).toString()
            )
        )
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                hideLoading(isUpdate, context)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.connection_problem),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val builder = GsonBuilder().create()
                val mJson = builder.fromJson(body, LessonJSONStructure::class.java)
                if (mJson.error != null) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "API response error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (context is PickerActivity)
                        context.hideLoading()
                    return
                }

                if (mJson.pairs.isNotEmpty()) {
                    if (context is PickerActivity)
                        context.runOnUiThread {
                            DatabaseHelper.addInformationToDBFromJSON(mJson)
                            DatabaseHelper.setVersion(mJson.version)
                            context.setDataVisible()
                        }
                    else if (context is MainActivity)
                        context.runOnUiThread {
                            DatabaseHelper.addInformationToDBFromJSON(mJson)
                            DatabaseHelper.setVersion(mJson.version)
                            context.onItemClicked(
                                context.bottomRecyclerAdapter.selectedDay,
                                true
                            )
                        }
                }
                hideLoading(isUpdate, context)
            }
        })
    }

    fun checkUpdate(activity: AppCompatActivity) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(URL("https://makson-dev.ru/api/getAndroidVersion.php?n=Nikita&last_version=${BuildConfig.VERSION_CODE}"))
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (activity is SettingsActivity)
                    activity.runOnUiThread {
                        Snackbar.make(
                            activity.mainLayout,
                            R.string.connection_problem,
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.try_again) {
                            checkUpdate(activity)
                        }.show()
                    }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val json = GsonBuilder().create().fromJson(body, UpdateJSONScheme::class.java)
                if (!json.status.isNullOrBlank()) {
                    if (activity is SettingsActivity)
                        activity.runOnUiThread {
                            Snackbar.make(
                                activity.mainLayout,
                                json.status,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    return
                }
                lastDownloadController = DownloadController(activity, json.link)
                Handler(Looper.getMainLooper()).post {
                    UpdateDialogFragment.newInstance(json.size, json.changelog).show(
                        activity.supportFragmentManager,
                        "update_dialog"
                    )
                }
            }
        })
    }
}