package com.example.schedule.activities

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
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.MarginItemDecoration
import com.example.schedule.OnSwipeTouchListener
import com.example.schedule.R
import com.example.schedule.URLRequests
import com.example.schedule.adapters.BottomRecycleAdapter
import com.example.schedule.adapters.MainRecycleAdapter
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.model.PairClass
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    lateinit var bottomRecycleAdapter: BottomRecycleAdapter
    private lateinit var mainRecycleAdapter: MainRecycleAdapter
    private lateinit var mPreference: SharedPreferences
    private var currentWeek: Int = 0
    private lateinit var arrayForMainRecyclerView: Array<PairClass>
    private lateinit var rotate: RotateAnimation

    companion object {
        const val CHANNEL_ID = "scheduleNotification"
        const val COUNT_LINES_REQUEST_CODE = 1
        const val GROUP_PICK_REQUEST_CODE = 2
        const val BOTH_REQUEST_CODE = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DatabaseHelper.init(this)
        realm = DatabaseHelper.getConnection()
        URLRequests.getJSON(this)
        createNotificationChannel()
        val currentDay = getCurrentDay()
        setBottomRecyclerView(currentDay)
        setMainRecyclerView(currentDay)
        setToggleAction()
        recycleViewMain.setOnTouchListener(getMainSwipeListener())
        initRotateForSettings()
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
        arrayForMainRecyclerView = Array(7) { PairClass() }

        recycleViewMain.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)

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
            if (isGroup) DatabaseHelper.getPairsOfGroup(savedValueOfUsersPick, currentDay + 1, even)
            else DatabaseHelper.getPairsOfLecturer(savedValueOfUsersPick, currentDay + 1, even)

        arrayForMainRecyclerView.map { it.clear() }

        for (pair in pairsData) {
            try {
                if (arrayForMainRecyclerView[pair.number - 1].group == "") {
                    arrayForMainRecyclerView[pair.number - 1].studyroom = pair.studyroom
                    arrayForMainRecyclerView[pair.number - 1].day = pair.day
                    arrayForMainRecyclerView[pair.number - 1].even = pair.even
                    arrayForMainRecyclerView[pair.number - 1].group = pair.group
                    arrayForMainRecyclerView[pair.number - 1].lecturer = pair.lecturer
                    arrayForMainRecyclerView[pair.number - 1].number = pair.number
                    arrayForMainRecyclerView[pair.number - 1].name = pair.name
                    arrayForMainRecyclerView[pair.number - 1].name = pair.name
                    arrayForMainRecyclerView[pair.number - 1].type = pair.type
                } else
                    arrayForMainRecyclerView[pair.number - 1].group += ", ${pair.group}"

            } catch (e: ArrayIndexOutOfBoundsException) {
                Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show()
            }
        }
        return arrayForMainRecyclerView
    }

    private fun setBottomRecyclerView(currentDay: Int) {
        recycleViewBottom.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )

        bottomRecycleAdapter =
            BottomRecycleAdapter(initAllWeekDates(), this, currentDay, toggle.isChecked)
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

    fun openSettings(view: View) {
        val intent = Intent(view.context, SettingsActivity::class.java)
        startActivityForResult(intent, COUNT_LINES_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        settingsButton.startAnimation(rotate)
        if (resultCode == GROUP_PICK_REQUEST_CODE)
            onSourceChange()
        else if (requestCode == BOTH_REQUEST_CODE || requestCode == COUNT_LINES_REQUEST_CODE)
            setMainRecyclerView(bottomRecycleAdapter.selectedDay)
    }

    private fun onSourceChange() {
        val pairsData =
            preparePairsData(
                bottomRecycleAdapter.selectedDay,
                if (toggle.isChecked) currentWeek else getNegativeWeek(getParityOfWeek())
            )
        mainRecycleAdapter.isGroup = mPreference.getBoolean(getString(R.string.isGroupPicked), true)
        mainRecycleAdapter.pairsData = pairsData
        mainRecycleAdapter.notifyDataSetChanged()
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

    private fun getDayName(pos: Int) =
        when (pos) {
            0 -> (getString(R.string.monday))
            1 -> (getString(R.string.tuesday))
            2 -> (getString(R.string.wednesday))
            3 -> (getString(R.string.thursday))
            4 -> (getString(R.string.friday))
            5 -> (getString(R.string.saturday))
            else -> (getString(R.string.sunday))
        }


    private fun getNegativeWeek(x: Int) = when (x) {
        1 -> 0
        else -> 1
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
        if (currentWeek % 2 != 0) {
            toggle.textOn = getString(R.string.current_week)
            toggle.textOff = getString(R.string.next_week)
            toggle.isChecked = false
        }
        return currentWeek
    }

    private fun initRotateForSettings() {
        rotate = RotateAnimation(
            0f,
            120f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 600
        rotate.interpolator = LinearInterpolator()
    }

    override fun onDestroy() {
        DatabaseHelper.closeConnection()
        super.onDestroy()
    }
}