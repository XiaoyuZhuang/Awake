package com.xiaoyu.awake

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(32, 32, 32, 32)

        listOf(
            "Timer duration",
            "Reminder interval",
            "Vibration",
            "Sound",
            "Notification",
            "Language",
            "Theme",
            "Startup mode",
            "Check update"
        ).forEach {
            root.addView(TextView(this).apply {
                text = it
                textSize = 18f
                setPadding(0, 24, 0, 24)
            })
        }

        setContentView(root)
    }
}
