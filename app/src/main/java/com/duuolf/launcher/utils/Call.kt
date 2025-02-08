package com.duuolf.launcher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri


// 拨打电话的工具函数
fun makeCall(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }catch (e:SecurityException){
        e.printStackTrace()
    }
}