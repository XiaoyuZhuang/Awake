package com.xiaoyu.awake

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.view.Gravity
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
        fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
        fun styleControl(view: android.view.View) {
            view.backgroundTintList = null
            view.setBackgroundResource(R.drawable.settings_control)
            view.setPadding(dp(16), dp(12), dp(16), dp(12))
            view.minimumHeight = dp(56)
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
        }
        fun button(text: String, action: () -> Unit) {
            root.addView(Button(this).apply {
                this.text = text
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                isAllCaps = false
                styleControl(this)
                setOnClickListener { action() }
            })
        }
        fun choice(title: String, items: Array<String>, index: Int, save: (Int) -> Unit) {
            var selected = index
            val control = Button(this).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                isAllCaps = false
                styleControl(this)
            }
            fun refresh() { control.text = title + "：" + items[selected] + "  ▾" }
            refresh()
            control.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle(title)
                    .setSingleChoiceItems(items, selected) { dialog, which ->
                        if (which != selected) {
                            selected = which
                            refresh()
                            save(which)
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(tr("取消", "Cancel"), null)
                    .show()
            }
            root.addView(control)
        }
        fun refreshService() {
            if (getSharedPreferences("awake_runtime", 0).getBoolean("active", false)) {
                ContextCompat.startForegroundService(this, Intent(this, TimerService::class.java).apply {
                    action = "REFRESH_SETTINGS"
                })
            }
        }
        fun number(text: String, current: () -> Long, max: Long, save: (Long) -> Unit) {
            val control = Button(this).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                isAllCaps = false
                styleControl(this)
            }
            fun refresh() { control.text = text + "：" + current() }
            refresh()
            control.setOnClickListener {
                val input = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    setText(current().toString())
                    selectAll()
                }
                val inputBox = LinearLayout(this).apply {
                    setPadding(dp(24), dp(8), dp(24), dp(8))
                    addView(input, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                }
                val dialog = AlertDialog.Builder(this)
                    .setTitle(text).setView(inputBox)
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
                styleControl(this)
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
        choice(tr("进入方式", "Entry mode"), arrayOf(tr("仅通过通知进入", "Open from notification only"),
            tr("每次启动后进入设置", "Open settings after every launch")),
            if (AppSettings.settingsEntry(this) == "every_launch") 1 else 0) {
            AppSettings.setSettingsEntry(this, if (it == 1) "every_launch" else "notification")
        }
        label(tr("外观", "Appearance"))
        choice(tr("语言", "Language"), arrayOf("简体中文", "English"), if (english) 1 else 0) {
            AppSettings.setLanguage(this, if (it == 1) "en" else "zh")
            recreate()
        }
        choice(tr("主题", "Theme"), arrayOf(tr("跟随系统", "System"), tr("浅色", "Light"), tr("黑色", "Black")),
            when (AppSettings.theme(this)) { "light" -> 1; "dark" -> 2; else -> 0 }) {
            AppSettings.setTheme(this, arrayOf("system", "light", "dark")[it])
            ThemeManager.apply(this)
        }
        label(tr("操作", "Actions"))
        button(tr("立即增加倒计时", "Add time now")) {
            ContextCompat.startForegroundService(this, Intent(this, TimerService::class.java).apply { action = "ADD_TIME" })
        }
        button(tr("停止倒计时与提醒", "Stop timer and reminders")) {
            startService(Intent(this, TimerService::class.java).apply { action = "STOP_ALL" })
        }
        button(tr("检查更新", "Check for updates")) { UpdateChecker.check(this) }
        button(tr("通知权限设置", "Notification permission settings")) {
            startActivity(Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName))
        }
        label(tr("当前版本", "Current version"))
        root.addView(TextView(this).apply { text = "Awake " + packageManager.getPackageInfo(packageName, 0).versionName })
        val permissionPref = getSharedPreferences("awake_permissions", 0)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            !permissionPref.getBoolean("asked", false)) {
            permissionPref.edit().putBoolean("asked", true).apply()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}
