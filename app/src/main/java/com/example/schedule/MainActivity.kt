package com.example.schedule

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.schedule.model.MyJSONFile
import com.example.schedule.model.PairClass
import com.example.schedule.model.VersionClass
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.createObject
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.util.*
import android.content.SharedPreferences
import android.widget.TimePicker


class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    companion object {
        const val CHANNEL_ID = "com.example.schedule"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);



        createNotificationChannel()
        Realm.init(this)

        realm = Realm.getDefaultInstance()
        getJSON()


}

fun check(view: View){

    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 18)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)

//    startAlarm(c)
}




private fun getJSON() {
    val client = OkHttpClient()
    val url = URL("https://api.npoint.io/51288cb390c242f3e007")

    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
           Toast.makeText(applicationContext, "$e", Toast.LENGTH_LONG).show()
            println("MY TAG $e")
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            val builder = GsonBuilder().create()
            val myData = builder.fromJson(body, MyJSONFile::class.java)
            runOnUiThread {
                getVersionFromDB(myData.version, myData)
            }
        }
    })
}

    private fun updateDatabaseInfo(data: MyJSONFile){
        realm.executeTransaction { realm ->
            for (pair in data.pairs) {
                println("MY TAG ${pair.subjectName}")
                val localPair = realm.createObject<PairClass>()
                localPair.aud = pair.aud
                localPair.day = pair.day
                localPair.even = pair.even
                localPair.group = pair.group
                localPair.lecturer = pair.lecturer
                localPair.number = pair.number
                localPair.subjectName = pair.subjectName
            }
        }
    }



    fun getVersionFromDB(newVersion: Double, data: MyJSONFile){
        val currentVersion = realm.where(VersionClass::class.java).findFirst()

        if (currentVersion == null) {
            // перввый запуск
            realm.executeTransaction { realm ->
                val v = realm.createObject<VersionClass>()
                v.version = newVersion
            }
            println("MY TAG first start v=$newVersion")
            // upd all db
            updateDatabaseInfo(data)
        } else if (newVersion > currentVersion.version){
            // обновление данных
            realm.executeTransaction {
                currentVersion.version = newVersion
            }
            println("MY TAG version updated, now $newVersion")
            tryDeleteFromDB()
            updateDatabaseInfo(data)
        }

    }

    private fun tryDeleteFromDB(){
        realm.executeTransaction { realm ->
            realm.delete(PairClass::class.java)
        }

    }

    fun startSettings(view: View){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lightColor = Color.GREEN
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)



        }
    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}