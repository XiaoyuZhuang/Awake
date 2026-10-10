package com.xiaoyu.awake

import android.content.Context
import android.content.Intent
import android.net.Uri

object UpdateChecker {
    fun openRelease(context: Context){
        val uri=Uri.parse("https://github.com/XiaoyuZhuang/Awake/releases")
        context.startActivity(Intent(Intent.ACTION_VIEW,uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
