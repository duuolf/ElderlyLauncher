package com.duuolf.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.core.app.NotificationCompat
import com.duuolf.launcher.MainActivity
import com.duuolf.launcher.R
import java.util.*

class TTSService : Service() {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val binder = TTSBinder()

    // 通过 Binder 提供服务接口
    inner class TTSBinder : Binder() {
        fun getService(): TTSService = this@TTSService
    }

    override fun onCreate() {
        super.onCreate()

        // 创建通知渠道（确保前台服务在 Android 8.0 以上能运行）
        createNotificationChannel()

        // 初始化 TTS
        tts = TextToSpeech(this, OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = tts?.setLanguage(Locale.CHINESE)
                if (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsInitialized = true
                }
            }
        })

        // 创建前台通知
        val notification = createNotification()
        startForeground(1, notification)
    }

    // 创建通知渠道（仅适用于 Android 8.0 及以上）
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TTS_CHANNEL",
                "TTS 语音服务",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    // 创建前台通知
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "TTS_CHANNEL")
            .setContentTitle("TTS 服务")
            .setContentText("正在运行")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 设置图标
            .setContentIntent(pendingIntent)
            .build()
    }

    // 执行文本转语音
    fun speak(text: String,isVibrate: Boolean =false) {
        if (tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
                if (isVibrate) {
                    vibratePattern(this)
                }
        }
    }

    // 绑定服务
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止 TTS 并释放资源
        tts?.stop()
        tts?.shutdown()
    }
    //震动
    private fun vibratePattern(context: Context) {
        val pattern = longArrayOf(0, 400, 100, 500, 0) // 震动 200ms -> 停 100ms -> 震动 300ms -> 停 400ms
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1)) // -1 表示不重复
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

}
