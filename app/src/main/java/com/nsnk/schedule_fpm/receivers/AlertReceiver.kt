package com.nsnk.schedule_fpm.receivers

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.nsnk.schedule_fpm.R
import com.nsnk.schedule_fpm.activities.MainActivity
import com.nsnk.schedule_fpm.database.DatabaseHelper
import com.nsnk.schedule_fpm.model.CalendarHelper
import com.nsnk.schedule_fpm.model.CalendarHelper.getNegativeWeek
import com.nsnk.schedule_fpm.model.PairClass

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val realm = DatabaseHelper(context)
        val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
        val savedValueOfUsersPick =
            mPreference.getString(context.getString(R.string.savedValueOfUsersPick), "") ?: ""
        val isGroup = mPreference.getBoolean(context.getString(R.string.isGroupPicked), true)
        CalendarHelper.setNextDay()
        val pairs =
            if (isGroup) realm.getPairsOfGroup(
                savedValueOfUsersPick,
                CalendarHelper.currentDay,
                getNegativeWeek(CalendarHelper.parity)
            )
            else realm.getPairsOfLecturer(
                savedValueOfUsersPick,
                CalendarHelper.currentDay,
                getNegativeWeek(CalendarHelper.parity)
            )
        val bigText: String
        val infoText: String

        val numberOfPairs = pairs.size
        if (numberOfPairs == 0) {
            bigText = "Завтра занятий нет"
            infoText = bigText
        } else {
            val info = getNotificationMessage(pairs)
            infoText = info.first
            bigText = info.second
        }
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.putExtra("selected_day", CalendarHelper.currentDay)
        val pendingIntent =
            PendingIntent.getActivity(context, 2, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notif_main)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.mipmap.ic_launcher_round
                )
            )
            .setContentTitle("Расписание на завтра: $savedValueOfUsersPick")
            .setContentText(infoText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, notification)
        }
        realm.closeConnection()
    }


    private fun getNotificationMessage(lessons: List<PairClass>): Pair<String, String> {
        val map = Array(8) { "" }
        for (lesson in lessons)
            if (map[lesson.number - 1].isBlank())
                map[lesson.number - 1] =
                    "${lesson.title} ${if (lesson.type == 0) "лк." else "пр."} в ауд. ${lesson.studyroom}"
        val resultString = StringBuilder()
        for ((index, item) in map.withIndex())
            if (item.isNotBlank())
                resultString.append("${index + 1}) $item\n")
        val stringCount = when (val countOfLessons = map.count { it.isNotBlank() }) {
            1 -> "$countOfLessons занятие"
            2, 3, 4 -> "$countOfLessons занятия"
            else -> "$countOfLessons занятий"
        }
        val firstLessonTime = getStartTime(lessons.minByOrNull { it.number }?.number)
        val lastLessonTime = getEndTime(lessons.maxByOrNull { it.number }?.number)
        return Pair(
            "$stringCount с $firstLessonTime до $lastLessonTime",
            resultString.substring(0, resultString.length - 1)
        )
    }


    private fun getStartTime(i: Int?): String {
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

    private fun getEndTime(i: Int?): String {
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