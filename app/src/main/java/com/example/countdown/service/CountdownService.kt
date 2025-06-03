package com.example.countdown.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.LifecycleService
import com.example.countdown.MainActivity
import com.example.countdown.R
import com.example.countdown.data.CountdownRepository
import com.example.countdown.utils.TimeUtils
import kotlinx.coroutines.launch

class CountdownService : LifecycleService() {
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "countdown_channel"
        const val ACTION_TOGGLE_PAUSE = "action_toggle_pause"
        const val ACTION_RESET = "action_reset"
        const val ACTION_STOP_SERVICE = "action_stop_service"
        
        fun startService(context: Context) {
            val intent = Intent(context, CountdownService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, CountdownService::class.java)
            context.stopService(intent)
        }
    }
    
    private lateinit var repository: CountdownRepository
    private lateinit var notificationManager: NotificationManager
    
    override fun onCreate() {
        super.onCreate()
        
        repository = CountdownRepository(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("准备中...", false))
        
        // 监听倒计时状态变化
        lifecycleScope.launch {
            repository.countdownState.collect { state ->
                val timeText = if (state.currentMillis > 0) {
                    TimeUtils.formatTime((state.currentMillis / 1000).toInt())
                } else {
                    "00:00"
                }
                
                updateNotification(timeText, state.isRunning)
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_TOGGLE_PAUSE -> {
                toggleCountdown()
            }
            ACTION_RESET -> {
                resetCountdown()
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
        }
        
        return START_STICKY
    }
    
    private fun toggleCountdown() {
        val intent = Intent("com.example.countdown.TOGGLE_COUNTDOWN")
        sendBroadcast(intent)
    }
    
    private fun resetCountdown() {
        val intent = Intent("com.example.countdown.RESET_COUNTDOWN")
        sendBroadcast(intent)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "倒计时通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示倒计时状态"
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(timeText: String, isRunning: Boolean): android.app.Notification {
        // 点击通知打开应用
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 暂停/继续按钮
        val toggleIntent = Intent(this, CountdownService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 重置按钮
        val resetIntent = Intent(this, CountdownService::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = PendingIntent.getService(
            this, 2, resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 停止服务按钮
        val stopIntent = Intent(this, CountdownService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_24)
            .setContentTitle("倒计时器")
            .setContentText("剩余时间: $timeText")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
        
        // 添加操作按钮
        if (isRunning) {
            builder.addAction(
                R.drawable.ic_pause_24,
                "暂停",
                togglePendingIntent
            )
        } else {
            builder.addAction(
                R.drawable.ic_play_24,
                "开始",
                togglePendingIntent
            )
        }
        
        builder.addAction(
            R.drawable.ic_refresh_24,
            "重置",
            resetPendingIntent
        )
        
        builder.addAction(
            R.drawable.ic_close_24,
            "关闭",
            stopPendingIntent
        )
        
        return builder.build()
    }
    
    private fun updateNotification(timeText: String, isRunning: Boolean) {
        val notification = createNotification(timeText, isRunning)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    //override fun onBind(intent: Intent?): IBinder? {
    //    super.onBind(intent)
    //    return null
    //}

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(NOTIFICATION_ID)
    }
} 