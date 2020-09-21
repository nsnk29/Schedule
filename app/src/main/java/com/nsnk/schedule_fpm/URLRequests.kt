package com.nsnk.schedule_fpm

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.nsnk.schedule_fpm.activities.PickerActivity
import com.nsnk.schedule_fpm.interfaces.GetLessonsInterface
import com.nsnk.schedule_fpm.model.LessonJsonStructure
import okhttp3.*
import java.io.IOException
import java.net.URL


object URLRequests {
    private val mDispatcher = Dispatcher().apply { maxRequests = 1 }
    private val client = OkHttpClient.Builder().dispatcher(mDispatcher).build()


    fun getLessonsJson(
        context: Context,
        getLessonsInterface: GetLessonsInterface,
        isUpdate: Boolean = false
    ) {
        fun hideLoading(context: Context) {
            if (isUpdate && context is PickerActivity) {
                context.hideLoading()
            }
        }

        val version = PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(context.getString(R.string.version), 0).toString()
        val request = Request.Builder()
            .url(URL("https://api.npoint.io/aad887cc70459af7331e"))
            .get()
            .build()
        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    hideLoading(context)
                }

                override fun onResponse(call: Call, response: Response) {
                    val lessonJsonStructure: LessonJsonStructure
                    try {
                        lessonJsonStructure = GsonBuilder().create()
                            .fromJson(response.body?.string(), LessonJsonStructure::class.java)
                    } catch (error: JsonSyntaxException) {
                        hideLoading(context)
                        return
                    }
                    if (lessonJsonStructure.pairs.isNotEmpty())
                        getLessonsInterface.onLessonsReady(lessonJsonStructure)
                    hideLoading(context)
                }
            })
    }
}