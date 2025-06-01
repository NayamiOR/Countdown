package com.example.countdown.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 倒计时数据仓库
 * 负责倒计时状态的持久化存储和读取
 */
class CountdownRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _countdownState = MutableStateFlow(loadState())
    val countdownState: StateFlow<CountdownState> = _countdownState.asStateFlow()

    companion object {
        private const val PREFS_NAME = "countdown_prefs"
        private const val KEY_TOTAL_SECONDS = "total_seconds"
        private const val KEY_CURRENT_MILLIS = "current_millis"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_PAUSED_TIME = "paused_time"
    }

    /**
     * 从SharedPreferences加载状态
     */
    private fun loadState(): CountdownState {
        val totalSeconds = prefs.getInt(KEY_TOTAL_SECONDS, 60)
        val currentMillis = prefs.getLong(KEY_CURRENT_MILLIS, totalSeconds * 1000L)
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        val startTime = prefs.getLong(KEY_START_TIME, 0L)
        val pausedTime = prefs.getLong(KEY_PAUSED_TIME, 0L)

        Log.d("CountdownRepository", "Loaded state: total=$totalSeconds, current=$currentMillis, running=$isRunning")
        return CountdownState(totalSeconds, currentMillis, isRunning, startTime, pausedTime)
    }

    /**
     * 保存状态到SharedPreferences
     */
    fun saveState(state: CountdownState) {
        prefs.edit().apply {
            putInt(KEY_TOTAL_SECONDS, state.totalSeconds)
            putLong(KEY_CURRENT_MILLIS, state.currentMillis)
            putBoolean(KEY_IS_RUNNING, state.isRunning)
            putLong(KEY_START_TIME, state.startTime)
            putLong(KEY_PAUSED_TIME, state.pausedTime)
            apply()
        }
        _countdownState.value = state
        Log.d("CountdownRepository", "Saved state: total=${state.totalSeconds}, current=${state.currentMillis}, running=${state.isRunning}")
    }

    /**
     * 更新状态
     */
    fun updateState(update: (CountdownState) -> CountdownState) {
        val newState = update(_countdownState.value)
        saveState(newState)
    }
} 