package com.duuolf.launcher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.BatteryManager
import android.util.Log
import com.duuolf.launcher.data.settingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalTime

class Receiver(context: Context, private val callback: (String, Boolean) -> Unit) :
    BroadcastReceiver() {
    private var lastBatteryLevel: Int = 100
    private val batteryThreshold: Int =
        runBlocking { context.settingsDataStore.data.first().batteryThreshold }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            //声音广播
            "android.media.VOLUME_CHANGED_ACTION" -> volumeChanged(context)
            //电量广播
            Intent.ACTION_BATTERY_CHANGED -> batteryChanged(context, intent)
        }
    }

    private fun volumeChanged(context: Context?) {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val streams = listOf(
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_SYSTEM
        )
        for (stream in streams) {
            val maxVolume = audioManager.getStreamMaxVolume(stream)
            val currentVolume = audioManager.getStreamVolume(stream)
            if (currentVolume < maxVolume) {
                audioManager.setStreamVolume(stream, maxVolume, 0)
            }
        }
    }

    private fun batteryChanged(context: Context?, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isDND = runBlocking { context?.settingsDataStore?.data?.first()?.isDND!! }
        val isVibrate = runBlocking { context?.settingsDataStore?.data?.first()?.isVibrate!! }
        val startTime: Int =
            runBlocking { context?.settingsDataStore?.data?.first()?.dndStartTime!! }
        val endTime: Int = runBlocking { context?.settingsDataStore?.data?.first()?.dndEndTime!! }
        val newTime = LocalTime.now()
        val time: Int = newTime.hour * 60 + newTime.minute
        Log.d(
            "电量",
            "阈值$batteryThreshold 当前电量$level 充电状态$status 上次电量$lastBatteryLevel 免打扰$isDND 免打扰时间$endTime~$startTime 当前时间$time 震动$isVibrate"
        )
        if (level <= batteryThreshold && level < lastBatteryLevel && status != BatteryManager.BATTERY_STATUS_CHARGING && (!isDND || time in endTime..startTime)) {
            lastBatteryLevel = level
            Log.d("电量", "电量低于阈值，发出通知")
            callback.invoke("当前电量$level%, 请及时充电。", isVibrate)
        }
    }
}