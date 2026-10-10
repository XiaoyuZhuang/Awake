package com.xiaoyu.awake

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val pref = context.getSharedPreferences("awake_runtime", 0)
        if (pref.getBoolean("active", pref.getLong("remain", 0) > 0)) {
            ContextCompat.startForegroundService(context, Intent(context, TimerService::class.java))
        }
    }
}
