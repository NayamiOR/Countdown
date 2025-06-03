package com.example.countdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.countdown.data.CountdownRepository
import com.example.countdown.service.NotificationManager
import com.example.countdown.ui.screens.CountdownScreen
import com.example.countdown.ui.theme.CountdownTheme
import com.example.countdown.viewmodel.CountdownViewModel
import com.example.countdown.viewmodel.CountdownViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var repository: CountdownRepository
    private val viewModel: CountdownViewModel by viewModels {
        CountdownViewModelFactory(repository)
    }
    
    private var serviceReceiver: BroadcastReceiver? = null
    private lateinit var notificationManager: NotificationManager

    // 请求通知权限
    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，可以启动服务
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化Repository
        repository = CountdownRepository(applicationContext)
        
        // 初始化通知管理器
        notificationManager = NotificationManager(this, repository)
        lifecycle.addObserver(notificationManager)
        
        // 请求通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // 注册广播接收器
        registerServiceReceiver()
        
        setContent {
            CountdownTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CountdownScreen(viewModel = viewModel)
                }
            }
        }
    }
    
    private fun registerServiceReceiver() {
        serviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "com.example.countdown.TOGGLE_COUNTDOWN" -> {
                        viewModel.toggleCountdown()
                    }
                    "com.example.countdown.RESET_COUNTDOWN" -> {
                        viewModel.resetCountdown()
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction("com.example.countdown.TOGGLE_COUNTDOWN")
            addAction("com.example.countdown.RESET_COUNTDOWN")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(serviceReceiver, filter)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceReceiver?.let {
            unregisterReceiver(it)
        }
    }

    override fun onPause() {
        super.onPause()
        // 当Activity暂停时，保存当前状态（防止数据丢失）
        viewModel.saveCurrentState()
        android.util.Log.d("MainActivity", "Activity paused, saved state")
    }
}

