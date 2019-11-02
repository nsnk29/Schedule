package com.example.schedule.activities

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.MarginItemDecoration
import com.example.schedule.OnSwipeTouchListener
import com.example.schedule.R
import com.example.schedule.adapters.BottomRecycleAdapter
import com.example.schedule.adapters.MainRecycleAdapter
import com.example.schedule.database.database
import com.example.schedule.model.MyJSONFile
import com.example.schedule.model.PairClass
import com.google.gson.GsonBuilder
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private lateinit var bottomRecycleAdapter: BottomRecycleAdapter
    private lateinit var mainRecycleAdapter: MainRecycleAdapter
    private lateinit var mPreference: SharedPreferences
    lateinit var database: database
    private var currentWeek: Int = 0

    companion object {
        const val CHANNEL_ID = "scheduleNotification"
        const val COUNT_LINES_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRealm()
        getJSON()
        createNotificationChannel()
        val currentDay = getCurrentDay()
        setBottomRecyclerView(currentDay)
        setMainRecyclerView(currentDay)
        setToggleAction()
        recycleViewMain.setOnTouchListener(getMainSwipeListener())
    }

    private fun getMainSwipeListener(): OnSwipeTouchListener {
        return object : OnSwipeTouchListener(applicationContext) {
            override fun onSwipeLeft() {
                if (bottomRecycleAdapter.selectedDay != 5)
                    updateBottomRecycler(bottomRecycleAdapter.selectedDay + 1, false)
                else {
                    bottomRecycleAdapter.selectedDay = 0
                    toggle.isChecked = !toggle.isChecked
                }
            }

            override fun onSwipeRight() {
                if (bottomRecycleAdapter.selectedDay != 0)
                    updateBottomRecycler(bottomRecycleAdapter.selectedDay - 1, false)
                else {
                    bottomRecycleAdapter.selectedDay = 5
                    toggle.isChecked = !toggle.isChecked
                }
            }
        }
    }


    private fun setMainRecyclerView(currentDay: Int) {
        recycleViewMain.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )

        val pairsData =
            preparePairsData(
                currentDay,
                if (toggle.isChecked) currentWeek else getNegativeWeek(getParityOfWeek())
            )
        mainRecycleAdapter = MainRecycleAdapter(pairsData, this)
        mainRecycleAdapter.setHasStableIds(true)
        recycleViewMain.adapter = mainRecycleAdapter
        recycleViewMain.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun preparePairsData(currentDay: Int, even: Int): Array<PairClass> {
        val savedValueOfUsersPick =
            mPreference.getString(getString(R.string.savedValueOfUsersPick), "")
        val isGroup = mPreference.getBoolean(getString(R.string.isGroupPicked), true)

        val pairsData =
            if (isGroup) database.getPairsOfGroup(savedValueOfUsersPick, currentDay + 1, even)
            else database.getPairsOfLecturer(savedValueOfUsersPick, currentDay+1, even)

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

        bottomRecycleAdapter =
            BottomRecycleAdapter(initAllWeekDates(), this, currentDay)
        oneTimeDayNameSet(currentDay)

        recycleViewBottom.adapter = bottomRecycleAdapter
        recycleViewBottom.addItemDecoration(MarginItemDecoration(0))
        recycleViewBottom.setHasFixedSize(true)
        recycleViewBottom.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun setToggleAction() {
        toggle.setOnCheckedChangeListener(
            fun(_: CompoundButton, _: Boolean) {
                updateCurrentWeek()
            }
        )
    }

    private fun initRealm() {
        database = database(applicationContext)
        realm = database.getConnection()
    }


    private fun getJSON() {
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val client = OkHttpClient()

        val url = URL(
            "https://makson.f-dev.ru/api/getContent.php?v=${mPreference.getLong(
                "version",
                0
            )}&n=Nikita"
        )

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
                val mJson = builder.fromJson(body, MyJSONFile::class.java)
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "size = ${mJson.pairs.size} v = ${mPreference.getLong("version", 0)}",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (mJson.pairs.isNotEmpty()) {
                        database.addInformationToDBFromJSON(mJson)
                        database.setVersion(mJson.version)
                        Toast.makeText(applicationContext, "UPDATE", Toast.LENGTH_SHORT).show()
                        updateBottomRecycler(bottomRecycleAdapter.currentDay, true)
                    }
                }
            }
        })
    }


    fun openSettings(view: View) {
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
        if (cal.get(Calendar.DAY_OF_WEEK) == 1)
            cal.add(Calendar.DATE, 1)
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
        updateBottomRecycler(bottomRecycleAdapter.selectedDay, true)
    }

    fun updateBottomRecycler(position: Int, isNecessaryToUpdate: Boolean) {
        if (bottomRecycleAdapter.selectedDay != position || isNecessaryToUpdate) {
            bottomRecycleAdapter.selectedDay = position
            bottomRecycleAdapter.notifyDataSetChanged()
            weekDayText.text = getDayName(position)
            mainRecycleAdapter.pairsData =
                preparePairsData(
                    position,
                    if (toggle.isChecked) currentWeek else getNegativeWeek(currentWeek)
                )
            mainRecycleAdapter.notifyDataSetChanged()
        }
    }

    private fun getDayName(pos: Int): String {
        return when (pos) {
            0 -> (getString(R.string.monday))
            1 -> (getString(R.string.tuesday))
            2 -> (getString(R.string.wednesday))
            3 -> (getString(R.string.thursday))
            4 -> (getString(R.string.friday))
            5 -> (getString(R.string.saturday))
            else -> (getString(R.string.sunday))
        }
    }


    private fun getNegativeWeek(x: Int): Int {
        return when (x) {
            1 -> 0
            else -> 1
        }
    }

    private fun oneTimeDayNameSet(position: Int) {
        weekDayText.text = getDayName(position)
    }

    private fun getCurrentDay(): Int {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
        return if (day == -1) 0 else day
    }


    private fun getParityOfWeek(): Int {
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.DAY_OF_WEEK) == 1)
            cal.add(Calendar.DATE, 1)
        currentWeek = cal.get(Calendar.WEEK_OF_YEAR) % 2
        return currentWeek
    }

    override fun onDestroy() {
        database.closeConnection()
        super.onDestroy()
    }
}