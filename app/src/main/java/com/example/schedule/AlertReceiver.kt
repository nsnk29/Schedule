package com.example.schedule

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.example.schedule.database.DatabaseHelper
import java.util.*

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val database = DatabaseHelper(context)
        val realm = database.getConnection()

        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.SUNDAY
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR) % 2
        val currentDay = cal.get(Calendar.DAY_OF_WEEK)
        val even = if (currentWeek == 0) 1 else 0
        val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
        val savedValueOfUsersPick =
            mPreference.getString(context.getString(R.string.savedValueOfUsersPick), "")
        val isGroup = mPreference.getBoolean(context.getString(R.string.isGroupPicked), true)

        val pairs =
            if (isGroup) database.getPairsOfGroup(savedValueOfUsersPick, currentDay, even)
            else database.getPairsOfLecturer(savedValueOfUsersPick, currentDay, even)


        var result = ""
        for (pair in pairs) {
            result += pair.number.toString() + ") " + pair.name + " " + pair.studyroom + "\n"
        }
        val numberOfPairs = pairs.size
        var info = ""
        if (numberOfPairs == 0) {
            result = "Завтра нет занятий"
        } else {
            info = "c ${getStart(pairs[0]?.number)} до ${getEnd(pairs[pairs.size - 1]?.number)}"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_main)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.mipmap.ic_launcher_round
                )
            )
            .setContentTitle("Расписание на завтра $savedValueOfUsersPick")
            .setContentText("$numberOfPairs занятия $info")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        result
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .build()
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, notification)
        }

        realm.close()
    }

    private fun getStart(i: Int?): String {
        return when (i) {
            1 -> "8:00"
            2 -> "9:40"
            3 -> "11:30"
            4 -> "13:10"
            5 -> "15:00"
            6 -> "16:40"
            7 -> "18:20"
            else -> "20:00"
        }
    }

    private fun getEnd(i: Int?): String {
        return when (i) {
            1 -> "9:30"
            2 -> "11:10"
            3 -> "13:00"
            4 -> "14:40"
            5 -> "16:30"
            6 -> "18:10"
            7 -> "19:50"
            else -> "21:30"
        }
    }


    companion object {
        const val CHANNEL_ID = "scheduleNotification"
        const val NOTIFICATION_ID = 1
    }
}