package com.xiaoyu.awake

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TimerService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Timer engine will be expanded: configurable duration, notification countdown and reminders.
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
