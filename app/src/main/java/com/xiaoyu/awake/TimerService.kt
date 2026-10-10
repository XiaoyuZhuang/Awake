package com.xiaoyu.awake

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import kotlin.concurrent.fixedRateTimer

class TimerService : Service() {
    private var remain = 0L
    private var timer: CountDownTimer? = null
    private var reminderTimer: java.util.Timer? = null

    private val pref by lazy { getSharedPreferences("awake_runtime", 0) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        restoreTimer()
        startForeground(NotificationHelper.ID, NotificationHelper.notification(this, format(remain)))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ADD_TIME" -> addTime()
            "STOP_REMINDER" -> stopReminder()
        }
        return START_STICKY
    }

    private fun addTime() {
        stopReminder()
        remain += AppSettings.addMinutes(this) * 60_000
        saveTimer()
        timer?.cancel()
        timer = object : CountDownTimer(remain, 1000) {
            override fun onTick(ms: Long) {
                remain = ms
                saveTimer()
                updateNotification(ms)
            }

            override fun onFinish() {
                remain = 0
                saveTimer()
                startReminderLoop()
            }
        }.start()
    }

    private fun restoreTimer() {
        remain = pref.getLong("remain", 0)
        if (remain > 0) {
            timer = object : CountDownTimer(remain, 1000) {
                override fun onTick(ms: Long) {
                    remain = ms
                    saveTimer()
                    updateNotification(ms)
                }
                override fun onFinish() {
                    remain = 0
                    saveTimer()
                    startReminderLoop()
                }
            }.start()
        }
    }

    private fun updateNotification(ms: Long) {
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this)
                .notify(NotificationHelper.ID, NotificationHelper.notification(this, format(ms)))
        }
    }

    private fun saveTimer() {
        pref.edit().putLong("remain", remain).apply()
    }

    private fun startReminderLoop() {
        reminderTimer?.cancel()
        reminderTimer = fixedRateTimer(
            name = "awake-reminder",
            initialDelay = 0,
            period = AppSettings.intervalSeconds(this) * 1000L
        ) {
            ReminderHelper.notifyReminder(this@TimerService)
        }
    }

    private fun stopReminder() {
        reminderTimer?.cancel()
        reminderTimer = null
    }

    private fun format(ms: Long): String {
        val sec = ms / 1000
        return String.format("%02d:%02d", sec / 60, sec % 60)
    }

    override fun onDestroy() {
        timer?.cancel()
        stopReminder()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
