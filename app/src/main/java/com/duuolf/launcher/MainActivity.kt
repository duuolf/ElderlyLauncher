package com.duuolf.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.BroadcastReceiver
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.duuolf.launcher.page.AppNavHost
import com.duuolf.launcher.service.TTSService
import com.duuolf.launcher.ui.theme.LauncherTheme
import com.duuolf.launcher.utils.Receiver
import com.duuolf.launcher.utils.checkAndRequestBatteryOptimizationWhitelist
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var displayManager: DisplayManager
    private lateinit var displayListener: DisplayManager.DisplayListener
    private lateinit var receiver: BroadcastReceiver
    private var ttsService: TTSService? = null
    private var isBound = false
    private var isScreenOn = false

    // 创建 ServiceConnection 用于绑定服务
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TTSService.TTSBinder
            ttsService = binder.getService()
            isBound = true
            Log.d("TTS", "服务已绑定")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            ttsService = null
            isBound = false
            Log.d("TTS", "服务已断开")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(this.intent.action== Intent.ACTION_MAIN && this.intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT == 0) {
            this.onBackPressedDispatcher.addCallback(this){

            }
        }
        enableEdgeToEdge() // 启用全屏模式
        setContent {
            val navController = rememberNavController()
            checkAndRequestBatteryOptimizationWhitelist(this)
            LauncherTheme {
                AppNavHost(navController)
            }
        }

        // 立即设置最大音量
        setMaxVolume()

        // 启动并绑定 TTS 服务
        val ttsIntent = Intent(this, TTSService::class.java)
        bindService(ttsIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        //创建并注册广播接收器
        receiver = Receiver(this,::tts)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)//电量广播
            addAction("android.media.VOLUME_CHANGED_ACTION")//声音广播
        }
        registerReceiver(receiver, filter)

        // 获取 DisplayManager 来监听屏幕状态变化
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        // 创建并注册屏幕状态监听器
        displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {}

            override fun onDisplayChanged(displayId: Int) {
                if (displayId == Display.DEFAULT_DISPLAY) {
                    val display = displayManager.getDisplay(displayId)
                    if (display.state == Display.STATE_ON&&!isScreenOn) {
                        // 屏幕亮起时的处理
                        Log.d("监听", "屏幕已点亮")
                        screenOn()
                        isScreenOn= true
                    }else if (display.state == Display.STATE_OFF) {
                        Log.d("监听", "屏幕已熄灭")
                        isScreenOn= false
                    }
                }
            }

            override fun onDisplayRemoved(displayId: Int) {}
        }

        // 注册屏幕状态监听器
        displayManager.registerDisplayListener(displayListener, null)
    }

    // 调用 TTS 服务中的 speak 方法
    private fun tts(text: String,isVibrate: Boolean =false) {
        if (isBound) {
            ttsService?.speak(text, isVibrate)
        } else {
            Log.e("TTS", "服务未绑定")
        }
    }
    private fun screenOn(){
        //获取当前时间
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm EEEE", Locale.CHINA)
        val formattedTime = currentDateTime.format(formatter)
        tts("现在是：$formattedTime")
    }
    override fun onDestroy() {
        super.onDestroy()
        // 解绑服务
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        // 注销广播接收器
        unregisterReceiver(receiver)
        // 注销屏幕状态监听器
        displayManager.unregisterDisplayListener(displayListener)
    }
    //设置音量为最大
    private fun setMaxVolume() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)//媒体
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)//铃声
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)//通知
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)//系统
    }
}