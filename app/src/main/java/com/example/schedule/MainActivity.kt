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
import io.realm.kotlin.createObject
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.util.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.adapters.BottomRecycleAdapter
import com.example.schedule.adapters.MainRecycleAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.y
import android.R.attr.x
import android.graphics.Point
import android.view.Display
import android.content.SharedPreferences
import android.R.id.edit
import android.util.TypedValue










class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private lateinit var bottomRecycleAdapter: BottomRecycleAdapter
    private lateinit var mainRecycleAdapter: MainRecycleAdapter
    companion object {
        const val CHANNEL_ID = "com.example.schedule"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        createNotificationChannel()


        recycleViewBottom.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )
        bottomRecycleAdapter = BottomRecycleAdapter(initAllWeekDates(), this)
        recycleViewBottom.adapter = bottomRecycleAdapter
        recycleViewBottom.addItemDecoration(MarginItemDecoration(0))
        recycleViewBottom.setHasFixedSize(true)
        recycleViewBottom.overScrollMode = View.OVER_SCROLL_NEVER

        recycleViewMain.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        mainRecycleAdapter = MainRecycleAdapter()
        recycleViewMain.adapter = mainRecycleAdapter
        recycleViewMain.overScrollMode = View.OVER_SCROLL_NEVER




        Realm.init(this)

        realm = Realm.getDefaultInstance()
        getJSON()

        checkFirstStart()
}

    fun check(v:View){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }


    fun checkFirstStart(){
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val isAgain = sp.getBoolean("isAgain", false)
        if (!isAgain) {
//            Toast.makeText(applicationContext, "first time", Toast.LENGTH_LONG).show()
//            val e = sp.edit()
//            e.putBoolean("isAgain", true)
//            e.apply()
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = size.x
            val height = size.y
            Toast.makeText(applicationContext, "$width ", Toast.LENGTH_LONG).show()
        }
        val dimensionInPixel: Float = 1072F
        val dimensionInDp =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInPixel, resources.displayMetrics).toInt()
        println("MY TAG $dimensionInDp $dimensionInPixel")


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
            runOnUiThread {
                Toast.makeText(applicationContext, "Проблемы подключения к сети", Toast.LENGTH_LONG).show()
            }

            println("MY TAG $e")
        }

        override fun onResponse(call: Call, response: Response) {
            println("MY TAG response")
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

    private fun initAllWeekDates(): ArrayList<Int>{
        val allWeekDates: ArrayList<Int> = ArrayList(14)
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = GregorianCalendar.MONDAY
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        allWeekDates.add(cal.get(Calendar.DAY_OF_MONTH))
        for (i in 1..13) {
            cal.add(Calendar.DATE, 1)
            allWeekDates.add(cal.get(Calendar.DAY_OF_MONTH))
        }
        return (allWeekDates)
    }


    fun updateCurrentWeek(v: View){
        bottomRecycleAdapter.changeCurrentWeekDate()
        bottomRecycleAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}