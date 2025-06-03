package com.example.countdown

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import android.util.Log
import com.example.countdown.data.CountdownRepository

class CountdownApplication : Application(), DefaultLifecycleObserver {
    
    private lateinit var repository: CountdownRepository
    
    override fun onCreate() {
        super<Application>.onCreate()
        
        // 初始化Repository
        repository = CountdownRepository(this)
        
        // 监听应用进程生命周期
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        Log.d("CountdownApplication", "Application created")
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        
        // 当应用进入后台时，自动暂停倒计时
        val currentState = repository.countdownState.value
        if (currentState.isRunning) {
            repository.updateState { state ->
                state.copy(
                    isRunning = false,
                    pausedTime = state.totalSeconds * 1000L - state.currentMillis,
                    startTime = 0L,
                    autoPaused = true // 标记为自动暂停
                )
            }
            Log.d("CountdownApplication", "App went to background, countdown auto-paused")
        }
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("CountdownApplication", "App came to foreground")
    }
} 