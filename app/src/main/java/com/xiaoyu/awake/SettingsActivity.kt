package com.xiaoyu.awake

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        title = "Awake Settings"

        fun label(text: String) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = 18f
                setPadding(0, 20, 0, 8)
            })
        }

        fun edit(default: String, save: (Long) -> Unit): EditText {
            return EditText(this).apply {
                inputType = 2
                setText(default)
                setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        text.toString().toLongOrNull()?.let(save)
                    }
                }
            }
        }

        label("Timer duration / 增加时间 (minutes)")
        root.addView(edit(AppSettings.addMinutes(this).toString()) {
            AppSettings.setAddMinutes(this, it)
        })

        label("Reminder interval / 提醒间隔 (seconds)")
        root.addView(edit(AppSettings.intervalSeconds(this).toString()) {
            AppSettings.setIntervalSeconds(this, it)
        })

        fun switch(text: String, value: Boolean, save: (Boolean) -> Unit) {
            root.addView(Switch(this).apply {
                this.text = text
                isChecked = value
                setOnCheckedChangeListener { _, checked -> save(checked) }
            })
        }

        switch("Vibration / 震动", AppSettings.vibration(this)) {
            AppSettings.setVibration(this, it)
        }
        switch("Sound / 声音", AppSettings.sound(this)) {
            AppSettings.setSound(this, it)
        }
        switch("Notification / 通知", AppSettings.notification(this)) {
            AppSettings.setNotification(this, it)
        }

        label("Language / 语言")
        root.addView(Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("简体中文", "English"))
            setSelection(if (AppSettings.language(this@SettingsActivity)=="en") 1 else 0)
        })

        label("Theme / 主题")
        root.addView(Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("System", "Light", "Dark"))
        })

        label("Startup mode / 启动方式")
        root.addView(Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Add time on launch", "Enter through notification"))
            setSelection(if (AppSettings.launchMode(this@SettingsActivity)=="NOTIFICATION_ONLY") 1 else 0)
            onItemSelectedListener = object: android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                    AppSettings.setLaunchMode(this@SettingsActivity,
                        if(pos==1) "NOTIFICATION_ONLY" else "ADD_TIME")
                }
            }
        })

        root.addView(Button(this).apply {
            text = "Check update / 检查更新"
            setOnClickListener { UpdateChecker.openReleasePage(this@SettingsActivity) }
        })

        setContentView(root)
    }
}
