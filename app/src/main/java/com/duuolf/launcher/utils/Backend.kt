package com.duuolf.launcher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast

// 直接在这里实现
@SuppressLint("ServiceCast")
fun checkAndRequestBatteryOptimizationWhitelist(context: Context) {
    // 获取 PowerManager 服务
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = context.packageName

    // 检查是否忽略电池优化（即应用是否在后台白名单中）
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        if (!isIgnoring) {
            // 不在白名单中，提示用户去设置
            requestBatteryOptimizationPermission(context)
        }
    } else {
        Toast.makeText(context, "当前设备版本不支持电池优化忽略", Toast.LENGTH_SHORT).show()
    }
}

// 跳转到设置页面，让用户手动加入白名单
@SuppressLint("BatteryLife")
private fun requestBatteryOptimizationPermission(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    intent.data = android.net.Uri.parse("package:${context.packageName}")
    context.startActivity(intent)
}
