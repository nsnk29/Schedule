package com.example.schedule.activities

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.MarginItemDecoration
import com.example.schedule.R
import com.example.schedule.adapters.BottomRecycleAdapter
import com.example.schedule.adapters.MainRecycleAdapter
import com.example.schedule.model.MyJSONFile
import com.example.schedule.model.PairClass
import com.example.schedule.model.VersionClass
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.kotlin.createObject
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private lateinit var bottomRecycleAdapter: BottomRecycleAdapter
    private lateinit var mainRecycleAdapter: MainRecycleAdapter

    companion object {
        const val CHANNEL_ID = "scheduleNotification"
        const val COUNT_LINES_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRealm()
        getJSON()
        setSwitchAction()
        createNotificationChannel()
        val currentDay = getCurrentDay()
        setBottomRecyclerView(currentDay)
        setMainRecyclerView(currentDay)

    }

    private fun setMainRecyclerView(currentDay: Int) {
        recycleViewMain.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )

        val pairsData =
            preparePairsData(currentDay, "46/1", getNegativeWeek(bottomRecycleAdapter.currentWeek))
        mainRecycleAdapter = MainRecycleAdapter(pairsData, this)
        recycleViewMain.adapter = mainRecycleAdapter
        recycleViewMain.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun preparePairsData(currentDay: Int, group: String, even: Int): Array<PairClass> {
        val pairsData = realm.where(PairClass::class.java).equalTo("group", group)
            .equalTo("day", currentDay + 1)
            .notEqualTo("even", even).findAll()


        val myArray = Array(7) { PairClass() }
        for (pair in pairsData) {
            try {
                myArray[pair.number - 1] = pair
            } catch (e: ArrayIndexOutOfBoundsException) {
                Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show()
            }
        }

        return myArray
    }


    private fun setBottomRecyclerView(currentDay: Int) {
        recycleViewBottom.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )
        val cal = Calendar.getInstance()

        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR) % 2
        bottomRecycleAdapter =
            BottomRecycleAdapter(initAllWeekDates(), this, currentDay, currentWeek)
        oneTimeDayNameSet(currentDay)

        recycleViewBottom.adapter = bottomRecycleAdapter
        recycleViewBottom.addItemDecoration(MarginItemDecoration(0))
        recycleViewBottom.setHasFixedSize(true)
        recycleViewBottom.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun setSwitchAction() {
        weekSwitch.setOnCheckedChangeListener(
            fun(_: CompoundButton, _: Boolean) {
                updateCurrentWeek()
            }
        )
    }

    private fun initRealm() {
        Realm.init(this)
        realm = Realm.getDefaultInstance()
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
                    Toast.makeText(
                        applicationContext,
                        "Проблемы подключения к сети",
                        Toast.LENGTH_LONG
                    ).show()
                }
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

    private fun updateDatabaseInfo(data: MyJSONFile) {
        realm.executeTransaction { realm ->
            for (pair in data.pairs) {
                val localPair = realm.createObject<PairClass>()
                localPair.studyroom = pair.studyroom
                localPair.day = pair.day
                localPair.even = pair.even
                localPair.group = pair.group
                localPair.lecturer = pair.lecturer
                localPair.number = pair.number
                localPair.name = pair.name
                localPair.type = pair.type
            }
        }
        updateBottomRecycler(bottomRecycleAdapter.currentDay, true)
    }


    fun getVersionFromDB(newVersion: Double, data: MyJSONFile) {
        val currentVersion = realm.where(VersionClass::class.java).findFirst()


        if (currentVersion == null) {
            // перввый запуск
            realm.executeTransaction { realm ->
                val v = realm.createObject<VersionClass>()
                v.version = newVersion
            }
            // upd all db
            updateDatabaseInfo(data)
        } else if (newVersion > currentVersion.version) {
            // обновление данных
            realm.executeTransaction {
                currentVersion.version = newVersion
            }
            tryDeleteFromDB()
            updateDatabaseInfo(data)
        }

    }

    private fun tryDeleteFromDB() {
        realm.executeTransaction { realm ->
            realm.delete(PairClass::class.java)
        }

    }

    fun startSettings(view: View) {
        val intent = Intent(view.context, SettingsActivity::class.java)
        startActivityForResult(intent, COUNT_LINES_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == COUNT_LINES_REQUEST_CODE) {
                val currentDay = getCurrentDay()

                setBottomRecyclerView(currentDay)
                setMainRecyclerView(currentDay)
            }
        }
    }
    private fun createNotificationChannel() {
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
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initAllWeekDates(): ArrayList<Int> {
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


    private fun updateCurrentWeek() {
        bottomRecycleAdapter.changeCurrentWeekDate()
        bottomRecycleAdapter.currentWeek = getNegativeWeek(bottomRecycleAdapter.currentWeek)
        updateBottomRecycler(bottomRecycleAdapter.selectedDay, true)
    }

    fun updateBottomRecycler(position: Int, isNess: Boolean) {
        if (bottomRecycleAdapter.selectedDay != position || isNess) {
            bottomRecycleAdapter.selectedDay = position
            bottomRecycleAdapter.notifyDataSetChanged()
            weekDayText.text = getDayName(position)
            mainRecycleAdapter.pairsData = preparePairsData(
                position,
                "46/1",
                getNegativeWeek(bottomRecycleAdapter.currentWeek)
            )
            mainRecycleAdapter.notifyDataSetChanged()
        }
    }

    private fun getDayName(pos: Int): String {
        return when (pos) {
            0 -> ("Понедельник")
            1 -> ("Вторник")
            2 -> ("Среда")
            3 -> ("Четверг")
            4 -> ("Пятница")
            5 -> ("Суббота")
            else -> ("Воскресенье")
        }
    }

    private fun getNegativeWeek(i: Int): Int {
        return when (i) {
            0 -> 1
            1 -> 0
            else -> 0
        }
    }

    private fun oneTimeDayNameSet(position: Int) {
        weekDayText.text = getDayName(position)
    }

    private fun getCurrentDay(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
    }
}