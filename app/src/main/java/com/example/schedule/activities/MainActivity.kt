package com.example.schedule.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION
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
import com.example.schedule.*
import com.example.schedule.adapters.BottomRecycleAdapter
import com.example.schedule.adapters.MainRecycleAdapter
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.interfaces.BottomRecyclerClickListener
import com.example.schedule.model.CalendarHelper
import com.example.schedule.model.CalendarHelper.getDayName
import com.example.schedule.model.CalendarHelper.getNegativeWeek
import com.example.schedule.model.PairClass
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomRecyclerClickListener {

    private lateinit var realm: Realm
    lateinit var bottomRecyclerAdapter: BottomRecycleAdapter

    private lateinit var mainRecyclerAdapter: MainRecycleAdapter
    private lateinit var mPreference: SharedPreferences
    private val arrayForMainRecyclerView: Array<PairClass> = Array(8) { PairClass() }
    private lateinit var rotate: RotateAnimation

    object CODES {
        const val CHANNEL_ID = "scheduleNotification"
        const val COUNT_LINES_RESULT_CODE = 1
        const val GROUP_PICK_RESULT_CODE = 2
        const val BOTH_RESULT_CODE = 3
        const val SETTINGS_ACTIVITY_REQUEST_CODE = 4
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState != null) {
            restoreData(savedInstanceState)
        }

        DatabaseHelper.init(this)
        realm = DatabaseHelper.getConnection()
        createNotificationChannel()
        mPreference = PreferenceManager.getDefaultSharedPreferences(this)

        setBottomRecyclerView(CalendarHelper.currentDay)
        setMainRecyclerView(CalendarHelper.currentDay)
        setToggleAction()
        recyclerViewMain.setOnTouchListener(getMainSwipeListener())
        initRotateForSettings()
        if (savedInstanceState == null) {
            URLRequests.checkUpdate(this)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putParcelableArray("pairsData", mainRecyclerAdapter.pairsData)
            putIntegerArrayList(
                "allWeekDates",
                bottomRecyclerAdapter.allWeekDates as ArrayList<Int>
            )
            putInt("currentDay", bottomRecyclerAdapter.currentDay)
            putInt("selectedDay", bottomRecyclerAdapter.selectedDay)
            putBoolean("isChecked", toggle.isChecked)
        }
        super.onSaveInstanceState(outState)
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

    @Suppress("UNCHECKED_CAST")
    private fun restoreData(savedInstanceState: Bundle) {
        toggle.isChecked = savedInstanceState.getBoolean("isChecked")
        setToggleTitles()
        val allWeekDates = savedInstanceState.getIntegerArrayList("allWeekDates") as List<Int>
        val currentDay = savedInstanceState.getInt("currentDay")
        val selectedDay = savedInstanceState.getInt("selectedDay")

        setWeekDay(selectedDay)
        bottomRecyclerAdapter =
            BottomRecycleAdapter(allWeekDates, this, currentDay, toggle.isChecked, this)
        bottomRecyclerAdapter.selectedDay = selectedDay

        val savedArray = savedInstanceState.getParcelableArray("pairsData")
        mainRecyclerAdapter = MainRecycleAdapter(
            Array(savedArray?.size ?: 0) { savedArray?.get(it) as PairClass },
            this
        )
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun setMainRecyclerView(currentDay: Int) {
        if (!this::mainRecyclerAdapter.isInitialized)
            mainRecyclerAdapter = getMainAdapter(currentDay)
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


    private fun setBottomRecyclerView(day: Int) {
        if (!this::bottomRecyclerAdapter.isInitialized) {
            initBottomRecycler(day)
        }
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

    private fun initBottomRecycler(day: Int) {
        setToggleTitles()
        bottomRecyclerAdapter =
            BottomRecycleAdapter(
                CalendarHelper.initAllWeekDates(),
                this,
                day,
                toggle.isChecked,
                this
            )
        setWeekDay(day)
    }

    private fun getMainAdapter(currentDay: Int): MainRecycleAdapter = MainRecycleAdapter(
        preparePairsData(
            currentDay,
            if (toggle.isChecked) CalendarHelper.parity else getNegativeWeek(CalendarHelper.parity)
        ), this
    )

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
            CODES.SETTINGS_ACTIVITY_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        settingsButton.startAnimation(rotate)
        when (resultCode) {
            CODES.GROUP_PICK_RESULT_CODE -> onSourceChange()
            CODES.COUNT_LINES_RESULT_CODE -> {
                mainRecyclerAdapter = getMainAdapter(bottomRecyclerAdapter.selectedDay)
                recyclerViewMain.adapter = mainRecyclerAdapter
            }
            CODES.BOTH_RESULT_CODE -> {
                mainRecyclerAdapter = getMainAdapter(bottomRecyclerAdapter.currentDay)
                recyclerViewMain.adapter = mainRecyclerAdapter
                onSourceChange()
            }
        }
    }

    private fun onSourceChange() {
        mainRecyclerAdapter.isGroup =
            mPreference.getBoolean(getString(R.string.isGroupPicked), true)
        updateMainRecyclerView(bottomRecyclerAdapter.selectedDay)
    }

    private fun createNotificationChannel() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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


    private fun setWeekDay(position: Int) {
        weekDayText.text = getDayName(this, position)
    }


    private fun setToggleTitles() {
        if (CalendarHelper.parity % 2 != 0) {
            toggle.textOn = getString(R.string.current_week)
            toggle.textOff = getString(R.string.next_week)
        } else {
            toggle.textOn = getString(R.string.next_week)
            toggle.textOff = getString(R.string.current_week)
        }
        toggle.text = if (toggle.isChecked) toggle.textOn else toggle.textOff
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
        DatabaseHelper.init(this)
        URLRequests.getLessonsJSON(this)
        if (CalendarHelper.updateCurrentInfo()) {
            initBottomRecycler(CalendarHelper.currentDay)
            setBottomRecyclerView(CalendarHelper.currentDay)
            mainRecyclerAdapter = getMainAdapter(CalendarHelper.currentDay)
            setMainRecyclerView(CalendarHelper.currentDay)
        }
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
        updateMainRecyclerView(position)
    }

    private fun updateMainRecyclerView(position: Int) {
        mainRecyclerAdapter.pairsData =
            preparePairsData(
                position,
                if (toggle.isChecked) CalendarHelper.parity else getNegativeWeek(CalendarHelper.parity)
            )
        mainRecyclerAdapter.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == DownloadController.PERMISSION_REQUEST_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                URLRequests.lastDownloadController?.enqueueDownload()
            } else {
                Snackbar.make(wrapper, R.string.storage_permission_denied, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }
}