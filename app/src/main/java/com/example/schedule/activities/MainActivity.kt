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
import com.example.schedule.interfaces.BottomRecyclerClickListener
import com.example.schedule.model.PairClass
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), BottomRecyclerClickListener {

    private lateinit var realm: Realm
    lateinit var bottomRecyclerAdapter: BottomRecycleAdapter


    private lateinit var mainRecyclerAdapter: MainRecycleAdapter
    private lateinit var mPreference: SharedPreferences
    private var currentWeek: Int = 0
    private val arrayForMainRecyclerView: Array<PairClass> = Array(8) { PairClass() }
    private lateinit var rotate: RotateAnimation

    object CODES {
        const val CHANNEL_ID = "scheduleNotification"
        const val COUNT_LINES_REQUEST_CODE = 1
        const val GROUP_PICK_REQUEST_CODE = 2
        const val BOTH_REQUEST_CODE = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        // toDo обработать onSavedInstance
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DatabaseHelper.init(this)
        realm = DatabaseHelper.getConnection()
        createNotificationChannel()
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val currentDay = getCurrentDay()
        setBottomRecyclerView(currentDay)
        setMainRecyclerView(currentDay)
        setToggleAction()
        recyclerViewMain.setOnTouchListener(getMainSwipeListener())
        initRotateForSettings()
    }

    private fun getMainSwipeListener(): OnSwipeTouchListener =
        object : OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeLeft() {
                if (bottomRecyclerAdapter.selectedDay != 5)
                    onItemClicked(bottomRecyclerAdapter.selectedDay + 1, true)
                else {
                    bottomRecyclerAdapter.selectedDay = 0
                    toggle.isChecked = !toggle.isChecked
                }
            }

            override fun onSwipeRight() {
                if (bottomRecyclerAdapter.selectedDay != 0)
                    onItemClicked(bottomRecyclerAdapter.selectedDay - 1, true)
                else {
                    bottomRecyclerAdapter.selectedDay = 5
                    toggle.isChecked = !toggle.isChecked
                }
            }
        }


    private fun setMainRecyclerView(currentDay: Int) {
        val pairsData =
            preparePairsData(
                currentDay,
                if (toggle.isChecked) currentWeek else getNegativeWeek(getParityOfWeek())
            )
        mainRecyclerAdapter = MainRecycleAdapter(pairsData, this)
        recyclerViewMain.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            adapter = mainRecyclerAdapter
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun preparePairsData(currentDay: Int, even: Int): Array<PairClass> {
        val savedValueOfUsersPick =
            mPreference.getString(getString(R.string.savedValueOfUsersPick), "") ?: ""
        val isGroup = mPreference.getBoolean(getString(R.string.isGroupPicked), true)

        val pairsData =
            if (isGroup) DatabaseHelper.getPairsOfGroup(savedValueOfUsersPick, currentDay + 1, even)
            else DatabaseHelper.getPairsOfLecturer(savedValueOfUsersPick, currentDay + 1, even)

        arrayForMainRecyclerView.map { it.clear() }

        for (pair in pairsData) {
            try {
                if (arrayForMainRecyclerView[pair.number - 1].group.isBlank())
                    arrayForMainRecyclerView[pair.number - 1].setFields(pair)
                else
                    arrayForMainRecyclerView[pair.number - 1].group += ", ${pair.group}"

            } catch (e: ArrayIndexOutOfBoundsException) {
                Toast.makeText(this, "JSON format error", Toast.LENGTH_SHORT).show()
            }
        }
        return arrayForMainRecyclerView
    }

    private fun setBottomRecyclerView(currentDay: Int) {
        bottomRecyclerAdapter =
            BottomRecycleAdapter(initAllWeekDates(), this, currentDay, toggle.isChecked, this)
        setWeekDay(currentDay)
        recyclerViewBottom.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.HORIZONTAL,
                false
            )
            adapter = bottomRecyclerAdapter
            addItemDecoration(MarginItemDecoration(0))
            setHasFixedSize(true)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun setToggleAction() {
        toggle.setOnCheckedChangeListener(
            fun(_: CompoundButton, _: Boolean) {
                bottomRecyclerAdapter.changeCurrentWeekDate()
                onItemClicked(bottomRecyclerAdapter.selectedDay, true)
            }
        )
    }

    fun openSettings(view: View) {
        startActivityForResult(
            Intent(view.context, SettingsActivity::class.java),
            CODES.COUNT_LINES_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        settingsButton.startAnimation(rotate)
        if (resultCode == CODES.GROUP_PICK_REQUEST_CODE)
            onSourceChange()
        else if (requestCode == CODES.BOTH_REQUEST_CODE || requestCode == CODES.COUNT_LINES_REQUEST_CODE)
            setMainRecyclerView(bottomRecyclerAdapter.selectedDay)
    }

    private fun onSourceChange() {
        mainRecyclerAdapter.isGroup =
            mPreference.getBoolean(getString(R.string.isGroupPicked), true)
        onItemClicked(bottomRecyclerAdapter.selectedDay, true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CODES.CHANNEL_ID, name, importance).apply {
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
        cal.apply {
            firstDayOfWeek = GregorianCalendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
            set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        }

        for (i in 0 until 14) {
            allWeekDates.add(cal.get(Calendar.DAY_OF_MONTH))
            cal.add(Calendar.DATE, 1)
        }
        return (allWeekDates)
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


    private fun setWeekDay(position: Int) {
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


    override fun onResume() {
        super.onResume()
        URLRequests.getJSON(this)
        DatabaseHelper.init(this)
    }

    override fun onDestroy() {
        DatabaseHelper.closeConnection()
        super.onDestroy()
    }

    override fun onItemClicked(position: Int, needToUpdate: Boolean) {
        if (bottomRecyclerAdapter.selectedDay == position && !needToUpdate)
            return
        val oldSelected = bottomRecyclerAdapter.selectedDay
        bottomRecyclerAdapter.apply {
            selectedDay = position
            notifyItemChanged(oldSelected)
            notifyItemChanged(position)
        }
        setWeekDay(position)
        mainRecyclerAdapter.pairsData =
            preparePairsData(
                position,
                if (toggle.isChecked) currentWeek else getNegativeWeek(currentWeek)
            )
        mainRecyclerAdapter.notifyDataSetChanged()
    }
}