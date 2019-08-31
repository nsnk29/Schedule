package com.example.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.schedule.model.VersionClass
import io.realm.Realm

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Realm.init(context)

        val realm = Realm.getDefaultInstance()

        val ver = realm.where(VersionClass::class.java).findFirst()!!.version

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Расписание на завтра ver $ver")
            .setContentText("Завтра 4 пары до 14:40")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "1.301б Сети\n" +
                                "2.100с R\n" +
                                "3.Ф-ра\n" +
                                "4.Миков 129"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500))
            .build()
        with(NotificationManagerCompat.from(context)) {
            notify(1, notification)
        }

        realm.close()
    }

    companion object {
        const val CHANNEL_ID = "scheduleNotification"

    }
}