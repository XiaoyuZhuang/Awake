package com.xiaoyu.awake

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat

/**
 * A separate single-instance task keeps launcher taps out of the settings task.
 * Process both fresh launches and reused-launcher intents, as in TapCount.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processLaunch()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        processLaunch()
    }

    private fun processLaunch() {
        ContextCompat.startForegroundService(applicationContext,
            Intent(this, TimerService::class.java).apply { action = "ADD_TIME" })
        if (AppSettings.settingsEntry(this) == "every_launch") {
            startActivity(Intent(this, SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
        finish()
    }
}
