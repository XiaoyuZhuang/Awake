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

        fun addLabel(text: String) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = 18f
                setPadding(0, 20, 0, 8)
            })
        }

        fun addSwitch(text: String, checked: Boolean = true) {
            root.addView(Switch(this).apply {
                this.text = text
                this.isChecked = checked
            })
        }

        addLabel("Timer duration / 增加时间 (minutes)")
        root.addView(EditText(this).apply {
            hint = "10"
            inputType = 2
        })

        addLabel("Reminder interval / 提醒间隔 (seconds)")
        root.addView(EditText(this).apply {
            hint = "10"
            inputType = 2
        })

        addSwitch("Vibration / 震动")
        addSwitch("Sound / 声音")
        addSwitch("Notification / 通知")

        addLabel("Language / 语言")
        root.addView(Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("简体中文", "English"))
        })

        addLabel("Theme / 主题")
        root.addView(Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("System", "Light", "Dark"))
        })

        addLabel("Startup mode / 启动方式")
        root.addView(Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Add time on launch", "Enter through notification"))
        })

        root.addView(Button(this).apply {
            text = "Check update / 检查更新"
            setOnClickListener {
                UpdateChecker.openReleasePage(this@SettingsActivity)
            }
        })

        setContentView(root)
    }
}
