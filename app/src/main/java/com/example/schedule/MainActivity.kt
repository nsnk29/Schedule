package com.example.schedule

import android.app.Notification
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.schedule.model.MyJSONFile
import com.example.schedule.model.PairClass
import com.example.schedule.model.VersionClass
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.kotlin.createObject
import okhttp3.*
import okhttp3.internal.notify
import java.io.IOException
import java.net.URL
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private val CHANNEL_ID = "com.example.schedule"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        Realm.init(this)

        realm = Realm.getDefaultInstance()
        getJSON()

}

fun check(view: View){

//    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
//        .setSmallIcon(R.mipmap.ic_launcher)
//        .setContentTitle("Title")
//        .setContentText("Notification text")
//
//    val notificationId = Random.nextInt(0, 100)
//    with(NotificationManagerCompat.from(this)) {
//        notify(notificationId, builder.build())
//    }

    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon_background)
        .setContentTitle("Расписание на завтра")
        .setContentText("Завтра 4 пары до 14:40")
//        .setLargeIcon(R.drawable.ic_launcher_background)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("1. 301б Сети\n" +
                    "2.100с R\n" +
                    "3.Ф-ра\n" +
                    "4.Миков 129"))
        .build()
    with(NotificationManagerCompat.from(this)) {
        notify(1, notification)
    }
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