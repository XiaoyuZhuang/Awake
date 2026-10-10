package com.xiaoyu.awake

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        title = "Awake Settings"

        fun label(text:String){
            root.addView(TextView(this).apply { this.text=text; textSize=18f })
        }
        fun spinner(items:Array<String>, index:Int, save:(Int)->Unit){
            root.addView(Spinner(this).apply {
                adapter=ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, items)
                setSelection(index)
                onItemSelectedListener=object: AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(parent:AdapterView<*>?){ }
                    override fun onItemSelected(parent:AdapterView<*>?,view:android.view.View?,pos:Int,id:Long){ save(pos) }
                }
            })
        }

        label("Language / 语言")
        spinner(arrayOf("简体中文","English"), if(AppSettings.language(this)=="en") 1 else 0){
            AppSettings.setLanguage(this, if(it==1) "en" else "zh")
        }

        label("Theme / 主题")
        spinner(arrayOf("System","Light","Dark"), when(AppSettings.theme(this)) {"light"->1;"dark"->2;else->0}){
            AppSettings.setTheme(this, arrayOf("system","light","dark")[it])
            ThemeManager.apply(this)
        }

        label("Other settings are preserved in AppSettings")
        root.addView(Button(this).apply {
            text="Check update / 检查更新"
            setOnClickListener { UpdateChecker.openReleasePage(this@SettingsActivity) }
        })
        setContentView(root)
    }
}
