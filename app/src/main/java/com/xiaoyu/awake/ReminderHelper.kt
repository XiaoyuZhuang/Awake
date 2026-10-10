package com.xiaoyu.awake

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object ReminderHelper {
    private const val ID = 1002
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null

    fun notifyReminder(context: Context) {
        val english = AppSettings.language(context) == "en"
        if (AppSettings.notification(context) &&
            (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED)) {
            context.getSystemService(NotificationManager::class.java).notify(ID,
                NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("Awake")
                    .setContentText(if (english) "Time is up — tap to open settings" else "倒计时结束，点击进入设置")
                    .setContentIntent(NotificationHelper.settingsIntent(context))
                    .setSilent(true)
                    .setAutoCancel(true)
                    .build())
        }
        if (AppSettings.sound(context)) {
            ringtone?.stop()
            ringtone = RingtoneManager.getRingtone(context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            ringtone?.play()
            val playing = ringtone
            handler.postDelayed({ playing?.stop() }, 1500)
        }
        if (AppSettings.vibration(context)) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun stop(context: Context) {
        handler.removeCallbacksAndMessages(null)
        ringtone?.stop()
        ringtone = null
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).cancel()
        context.getSystemService(NotificationManager::class.java).cancel(ID)
    }
}
