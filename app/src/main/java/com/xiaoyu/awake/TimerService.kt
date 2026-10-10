package com.xiaoyu.awake

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class TimerService : Service() {
    private var deadline = 0L
    private var timer: CountDownTimer? = null
    private var reminding = false
    private val handler = Handler(Looper.getMainLooper())
    private val pref by lazy { getSharedPreferences("awake_runtime", 0) }

    private val reminder = object : Runnable {
        override fun run() {
            if (!reminding) return
            ReminderHelper.notifyReminder(this@TimerService)
            handler.postDelayed(this, AppSettings.intervalSeconds(this@TimerService).coerceIn(1, 3600) * 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        val savedRemain = pref.getLong("remain", 0)
        deadline = pref.getLong("deadline", if (savedRemain > 0) System.currentTimeMillis() + savedRemain else 0)
        startForeground(NotificationHelper.ID, NotificationHelper.notification(this, format(remaining())))
        if (deadline > 0) {
            startCountdown()
        } else if (pref.getBoolean("reminding", false)) {
            startReminderLoop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ADD_TIME" -> {
                val minutes = AppSettings.addMinutes(this).coerceIn(1, 1440)
                val duration = remaining() + minutes * 60_000L
                stopReminder()
                deadline = System.currentTimeMillis() + duration
                startCountdown()
                NotificationHelper.timeAdded(this, format(remaining()), minutes)
            }
            "STOP_ALL", "STOP_REMINDER" -> {
                timer?.cancel()
                timer = null
                deadline = 0
                stopReminder()
                pref.edit().putLong("remain", 0).putLong("deadline", 0)
                    .putBoolean("active", false).apply()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            "REFRESH_SETTINGS" -> if (reminding) {
                handler.removeCallbacks(reminder)
                ReminderHelper.stop(this)
                handler.postDelayed(reminder, AppSettings.intervalSeconds(this) * 1000L)
            }
        }
        return START_STICKY
    }

    private fun remaining() = (deadline - System.currentTimeMillis()).coerceAtLeast(0)

    private fun startCountdown() {
        timer?.cancel()
        val duration = remaining()
        pref.edit().putLong("deadline", deadline).putLong("remain", duration)
            .putBoolean("active", true).apply()
        if (duration <= 0) {
            startReminderLoop()
            return
        }
        updateNotification(format(duration))
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(ms: Long) {
                pref.edit().putLong("remain", remaining()).apply()
                updateNotification(format(remaining()))
            }
            override fun onFinish() { startReminderLoop() }
        }.start()
    }

    private fun startReminderLoop() {
        timer?.cancel()
        timer = null
        deadline = 0
        handler.removeCallbacks(reminder)
        reminding = true
        pref.edit().putLong("remain", 0).putLong("deadline", 0)
            .putBoolean("reminding", true).putBoolean("active", true).apply()
        updateNotification(if (AppSettings.language(this) == "en") "Time is up" else "倒计时结束")
        handler.post(reminder)
    }

    private fun stopReminder() {
        reminding = false
        handler.removeCallbacks(reminder)
        ReminderHelper.stop(this)
        pref.edit().putBoolean("reminding", false).apply()
    }

    private fun updateNotification(text: String) {
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NotificationHelper.ID, NotificationHelper.notification(this, text))
        }
    }

    private fun format(ms: Long): String {
        val seconds = (ms + 999) / 1000
        return String.format("%02d:%02d", seconds / 60, seconds % 60)
    }

    override fun onDestroy() {
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
        ReminderHelper.stop(this)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
