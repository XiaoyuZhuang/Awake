package com.xiaoyu.awake

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat

/**
 * Theme.NoDisplay handles launcher taps without creating a visible app window.
 * Notification permission is requested only from SettingsActivity.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            ContextCompat.startForegroundService(this,
                Intent(this, TimerService::class.java).apply { action = "ADD_TIME" })
            if (AppSettings.settingsEntry(this) == "every_launch") {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        finish()
    }
}
