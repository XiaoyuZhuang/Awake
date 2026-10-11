package com.xiaoyu.awake

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private val runtime by lazy { getSharedPreferences("awake_runtime", 0) }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var countdown: TextView
    private lateinit var timerState: TextView
    private lateinit var stopButton: Button
    private val runtimeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> renderTimer() }
    private val tick = object : Runnable {
        override fun run() {
            renderTimer()
            handler.postDelayed(this, 1000)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.apply(newBase))
    }

    private fun tr(zh: String, en: String) = if (AppSettings.language(this) == "en") en else zh
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        runtime.registerOnSharedPreferenceChangeListener(runtimeListener)
        handler.removeCallbacks(tick)
        tick.run()
    }

    override fun onPause() {
        runtime.unregisterOnSharedPreferenceChangeListener(runtimeListener)
        handler.removeCallbacks(tick)
        super.onPause()
    }

    private fun renderTimer() {
        val active = runtime.getBoolean("active", false)
        val deadline = runtime.getLong("deadline", 0)
        val remaining = if (active) (deadline - System.currentTimeMillis()).coerceAtLeast(0) else 0
        val seconds = (remaining + 999) / 1000
        countdown.text = String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
        timerState.text = when {
            !active -> tr("未启动", "Stopped")
            remaining > 0 -> tr("当前倒计时", "Time remaining")
            else -> tr("倒计时结束 · 正在提醒", "Time is up · Reminding")
        }
        stopButton.isEnabled = active
    }

    private fun refreshService() {
        if (runtime.getBoolean("active", false)) {
            ContextCompat.startForegroundService(this, Intent(this, TimerService::class.java).apply {
                action = "REFRESH_SETTINGS"
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(12))
        }
        scroll.addView(root)
        setContentView(scroll)
        ScreenLayout.apply(this, scroll)

        fun label(text: String) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = 15f
                setPadding(0, dp(8), 0, dp(4))
            })
        }
        fun style(view: View) {
            view.backgroundTintList = null
            view.setBackgroundResource(R.drawable.settings_control)
            view.setPadding(dp(12), dp(6), dp(12), dp(6))
            view.minimumHeight = dp(48)
            view.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) }
        }
        fun row(left: View, right: View) {
            root.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(left, LinearLayout.LayoutParams(0, -2, 1f).apply {
                    rightMargin = dp(4); bottomMargin = dp(6)
                })
                addView(right, LinearLayout.LayoutParams(0, -2, 1f).apply {
                    leftMargin = dp(4); bottomMargin = dp(6)
                })
            })
        }
        fun button(text: String, action: () -> Unit): Button = Button(this).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            isAllCaps = false
            minimumWidth = 0
            style(this)
            setOnClickListener { action() }
        }
        fun choice(title: String, items: Array<String>, index: Int, compact: Boolean = false,
                   save: (Int) -> Unit): Button {
            var selected = index
            val control = button("") {}
            fun refresh() {
                control.text = title + (if (compact) "\n" else "：") + items[selected] + " ▾"
            }
            refresh()
            control.setOnClickListener {
                AlertDialog.Builder(this).setTitle(title)
                    .setSingleChoiceItems(items, selected) { dialog, which ->
                        if (which != selected) {
                            selected = which
                            refresh()
                            save(which)
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(tr("取消", "Cancel"), null).show()
            }
            return control
        }
        fun number(title: String, current: () -> Long, max: Long, save: (Long) -> Unit) {
            val control = button("") {}
            control.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            fun refresh() { control.text = title + "：" + current() }
            refresh()
            control.setOnClickListener {
                val input = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    setText(current().toString())
                    selectAll()
                }
                val box = LinearLayout(this).apply {
                    setPadding(dp(20), dp(8), dp(20), dp(8))
                    addView(input, LinearLayout.LayoutParams(-1, -2))
                }
                val dialog = AlertDialog.Builder(this).setTitle(title).setView(box)
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
        fun toggle(title: String, initial: Boolean, save: (Boolean) -> Unit): SwitchCompat =
            SwitchCompat(this).apply {
                text = title
                textSize = 14f
                isChecked = initial
                switchPadding = dp(6)
                style(this)
                setOnCheckedChangeListener { _, checked -> save(checked); refreshService() }
            }

        timerState = TextView(this).apply {
            id = R.id.timer_state
            textSize = 14f
            gravity = Gravity.CENTER
        }
        countdown = TextView(this).apply {
            id = R.id.countdown_value
            textSize = 36f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, dp(6))
        }
        root.addView(timerState)
        root.addView(countdown)
        val add = button(tr("立即增加", "Add time")) {
            ContextCompat.startForegroundService(this, Intent(this, TimerService::class.java).apply {
                action = "ADD_TIME"
            })
        }.apply { id = R.id.add_time }
        stopButton = button(tr("停止倒计时", "Stop timer")) {
            startService(Intent(this, TimerService::class.java).apply { action = "STOP_ALL" })
        }.apply { id = R.id.stop_timer }
        row(add, stopButton)
        renderTimer()

        label(tr("倒计时与提醒", "Timer and reminders"))
        number(tr("每次启动增加的分钟数", "Minutes added per launch"),
            { AppSettings.addMinutes(this) }, 1440) { AppSettings.setAddMinutes(this, it) }
        number(tr("到时提醒间隔（秒）", "Reminder interval (seconds)"),
            { AppSettings.intervalSeconds(this) }, 3600) { AppSettings.setIntervalSeconds(this, it) }
        row(toggle(tr("震动", "Vibration"), AppSettings.vibration(this)) { AppSettings.setVibration(this, it) },
            toggle(tr("声音", "Sound"), AppSettings.sound(this)) { AppSettings.setSound(this, it) })
        root.addView(toggle(tr("到时通知", "Reminder notification"), AppSettings.notification(this)) {
            AppSettings.setNotification(this, it)
        })

        label(tr("设置页面入口", "Settings entry"))
        root.addView(choice(tr("进入方式", "Entry mode"),
            arrayOf(tr("仅通过通知进入", "Open from notification only"),
                tr("每次启动后进入设置", "Open settings after every launch")),
            if (AppSettings.settingsEntry(this) == "every_launch") 1 else 0) {
            AppSettings.setSettingsEntry(this, if (it == 1) "every_launch" else "notification")
        })
        root.addView(button(tr("添加桌面加时快捷方式", "Add home screen shortcut")) {
            if (!Shortcuts.pin(this)) {
                Toast.makeText(this, tr("当前桌面不支持添加快捷方式", "Your launcher does not support pinned shortcuts"),
                    Toast.LENGTH_LONG).show()
            }
        })

        label(tr("外观", "Appearance"))
        row(choice(tr("语言", "Language"), arrayOf("简体中文", "English"),
            if (AppSettings.language(this) == "en") 1 else 0, true) {
            AppSettings.setLanguage(this, if (it == 1) "en" else "zh")
            recreate()
        }, choice(tr("主题", "Theme"),
            arrayOf(tr("跟随系统", "System"), tr("浅色", "Light"), tr("黑色", "Black")),
            when (AppSettings.theme(this)) { "light" -> 1; "dark" -> 2; else -> 0 }, true) {
            AppSettings.setTheme(this, arrayOf("system", "light", "dark")[it])
            ThemeManager.apply(this)
        })

        label(tr("权限与更新", "Permissions and updates"))
        row(button(tr("通知权限", "Notifications")) {
            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
        }, button(tr("后台运行设置", "Background settings")) {
            try {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (_: android.content.ActivityNotFoundException) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        })
        root.addView(TextView(this).apply {
            textSize = 12f
            text = tr("常驻通知由本地前台服务维护，无需谷歌服务。若后台被清理，请允许自启动并取消电池限制。",
                "The ongoing notification runs locally without Google services. Allow autostart and unrestricted battery use if your phone stops it.")
        })
        root.addView(button(tr("检查更新", "Check for updates")) { UpdateChecker.check(this) })
        root.addView(TextView(this).apply {
            textSize = 12f
            gravity = Gravity.CENTER
            text = "Awake " + packageManager.getPackageInfo(packageName, 0).versionName
        })
        val permissionPref = getSharedPreferences("awake_permissions", 0)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            !permissionPref.getBoolean("asked", false)) {
            permissionPref.edit().putBoolean("asked", true).apply()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}
