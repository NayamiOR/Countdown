package com.example.countdown.service

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.countdown.data.CountdownRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class NotificationManager(
    private val context: Context,
    private val repository: CountdownRepository
) : DefaultLifecycleObserver {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isServiceRunning = false
    
    init {
        // 监听倒计时状态变化
        scope.launch {
            repository.countdownState
                .distinctUntilChangedBy { state -> 
                    Triple(state.isRunning, state.pausedTime > 0, state.currentMillis <= 0)
                }
                .collect { state ->
                    handleStateChange(state.isRunning, state.pausedTime > 0, state.currentMillis <= 0)
                }
        }
    }
    
    private fun handleStateChange(isRunning: Boolean, isPaused: Boolean, isFinished: Boolean) {
        val shouldShowNotification = isRunning || isPaused
        
        if (shouldShowNotification && !isServiceRunning) {
            // 需要显示通知但服务未运行，启动服务
            CountdownService.startService(context)
            isServiceRunning = true
        } else if (!shouldShowNotification && isServiceRunning && isFinished) {
            // 不需要显示通知且倒计时已结束，停止服务
            CountdownService.stopService(context)
            isServiceRunning = false
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        scope.cancel()
        if (isServiceRunning) {
            CountdownService.stopService(context)
        }
    }
    
    fun forceStopService() {
        if (isServiceRunning) {
            CountdownService.stopService(context)
            isServiceRunning = false
        }
    }
} 