package com.xiaoyu.awake

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
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

        if (AppSettings.soundEnabled(context)) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        context.getSystemService(NotificationManager::class.java)
            .notify(ID, builder.build())

        if (AppSettings.vibrationEnabled(context)) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            vibrator.vibrate(300)
        }
    }
}
