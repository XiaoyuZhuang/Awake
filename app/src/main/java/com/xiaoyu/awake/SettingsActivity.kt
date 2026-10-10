package com.xiaoyu.awake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.apply(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        super.onCreate(savedInstanceState)
        val english = AppSettings.language(this) == "en"
        fun tr(zh: String, en: String) = if (english) en else zh
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        scroll.addView(root)
        setContentView(scroll)
        ScreenLayout.apply(this, scroll)
        fun label(text: String) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = 18f
                setPadding(0, 24, 0, 8)
            })
        }
        fun button(text: String, action: () -> Unit) {
            root.addView(Button(this).apply {
                this.text = text
                setOnClickListener { action() }
            })
        }
        fun spinner(items: Array<String>, index: Int, save: (Int) -> Unit) {
            var lastIndex = index
            root.addView(Spinner(this).apply {
                adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, items)
                setSelection(index)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                        if (pos != lastIndex) { lastIndex = pos; save(pos) }
                    }
                }
            })
        }
        fun refreshService() {
            if (getSharedPreferences("awake_runtime", 0).getBoolean("active", false)) {
                ContextCompat.startForegroundService(this, Intent(this, TimerService::class.java).apply {
                    action = "REFRESH_SETTINGS"
                })
            }
        }
        fun number(text: String, current: () -> Long, max: Long, save: (Long) -> Unit) {
            val control = Button(this)
            fun refresh() { control.text = text + "：" + current() }
            refresh()
            control.setOnClickListener {
                val input = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    setText(current().toString())
                    selectAll()
                }
                val dialog = AlertDialog.Builder(this)
                    .setTitle(text).setView(input)
                    .setPositiveButton(tr("保存", "Save"), null)
                    .setNegativeButton(tr("取消", "Cancel"), null).create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val value = input.text.toString().toLongOrNull()
                        if (value == null || value !in 1..max) {
                            input.error = tr("请输入 1–$max 的整数", "Enter an integer from 1 to $max")
                        } else {
                            save(value)
                            refresh()
                            refreshService()
                            dialog.dismiss()
                        }
                    }
                }
                dialog.show()
            }
            root.addView(control)
        }
        fun toggle(text: String, initial: Boolean, save: (Boolean) -> Unit) {
            root.addView(androidx.appcompat.widget.SwitchCompat(this).apply {
                this.text = text
                isChecked = initial
                setPadding(0, 12, 0, 12)
                setOnCheckedChangeListener { _, checked -> save(checked); refreshService() }
            })
        }

        label(tr("Awake 设置", "Awake Settings"))
        root.addView(TextView(this).apply {
            text = tr("点击应用图标增加倒计时；点击常驻通知进入设置。", "Tap the app icon to add time; tap the ongoing notification to open settings.")
        })
        label(tr("倒计时与提醒", "Timer and reminders"))
        number(tr("每次启动增加的分钟数", "Minutes added per launch"),
            { AppSettings.addMinutes(this) }, 1440) { AppSettings.setAddMinutes(this, it) }
        number(tr("到时提醒间隔（秒）", "Reminder interval (seconds)"),
            { AppSettings.intervalSeconds(this) }, 3600) { AppSettings.setIntervalSeconds(this, it) }
        toggle(tr("震动反馈", "Vibration"), AppSettings.vibration(this)) { AppSettings.setVibration(this, it) }
        toggle(tr("声音反馈", "Sound"), AppSettings.sound(this)) { AppSettings.setSound(this, it) }
        toggle(tr("到时通知", "Reminder notification"), AppSettings.notification(this)) { AppSettings.setNotification(this, it) }

        label(tr("设置页面入口", "Settings entry"))
        spinner(arrayOf(tr("仅通过通知进入", "Open from notification only"),
            tr("每次启动后进入设置", "Open settings after every launch")),
            if (AppSettings.settingsEntry(this) == "every_launch") 1 else 0) {
            AppSettings.setSettingsEntry(this, if (it == 1) "every_launch" else "notification")
        }
        label(tr("外观", "Appearance"))
        spinner(arrayOf("简体中文", "English"), if (english) 1 else 0) {
            AppSettings.setLanguage(this, if (it == 1) "en" else "zh")
            recreate()
        }
        spinner(arrayOf(tr("跟随系统", "System"), tr("浅色", "Light"), tr("黑色", "Black")),
            when (AppSettings.theme(this)) { "light" -> 1; "dark" -> 2; else -> 0 }) {
            AppSettings.setTheme(this, arrayOf("system", "light", "dark")[it])
            ThemeManager.apply(this)
        }
        label(tr("操作", "Actions"))
        button(tr("立即增加倒计时", "Add time now")) {
            ContextCompat.startForegroundService(this, Intent(this, TimerService::class.java).apply { action = "ADD_TIME" })
            Toast.makeText(this, tr("已增加倒计时", "Time added"), Toast.LENGTH_SHORT).show()
        }
        button(tr("停止倒计时与提醒", "Stop timer and reminders")) {
            startService(Intent(this, TimerService::class.java).apply { action = "STOP_ALL" })
        }
        button(tr("检查更新", "Check for updates")) { UpdateChecker.check(this) }
        label(tr("当前版本", "Current version"))
        root.addView(TextView(this).apply { text = "Awake " + packageManager.getPackageInfo(packageName, 0).versionName })
    }
}
