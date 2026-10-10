package com.xiaoyu.awake

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object ReminderHelper {
    private const val ID = 1002

    fun remind(context: Context) {
        if (!AppSettings.notificationEnabled(context)) return

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Awake")
            .setContentText("Time is up")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        context.getSystemService(NotificationManager::class.java)
            .notify(ID, builder.build())

        if (AppSettings.vibrateEnabled(context)) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            vibrator.vibrate(300)
        }
    }
}
