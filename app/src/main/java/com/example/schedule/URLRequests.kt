package com.example.schedule

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.schedule.activities.MainActivity
import com.example.schedule.activities.PickerActivity
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.model.MyJSONFile
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import java.net.URL


object URLRequests {
    fun getJSON(context: Context) {
        val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
        val client = OkHttpClient()
        val url = URL(
            context.getString(R.string.URL_JSON).replace(
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

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Проблемы подключения к сети",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val builder = GsonBuilder().create()
                val mJson = builder.fromJson(body, MyJSONFile::class.java)
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
                            context.updateBottomRecycler(
                                context.bottomRecycleAdapter.currentDay,
                                true
                            )
                        }
                }
            }
        })
    }
}