package com.example.countdown.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        private const val KEY_TODAY_COMPLETED_SECONDS = "today_completed_seconds"
        private const val KEY_LAST_DATE = "last_date"
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
        val todayCompletedSeconds = prefs.getInt(KEY_TODAY_COMPLETED_SECONDS, 0)
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""

        val currentDate = getCurrentDate()
        
        // 如果日期变了，重置今日累计时长
        val finalTodayCompletedSeconds = if (lastDate != currentDate) {
            Log.d("CountdownRepository", "Date changed from $lastDate to $currentDate, resetting today's total")
            0
        } else {
            todayCompletedSeconds
        }

        Log.d("CountdownRepository", "Loaded state: total=$totalSeconds, current=$currentMillis, running=$isRunning, todayCompleted=$finalTodayCompletedSeconds")
        return CountdownState(
            totalSeconds, 
            currentMillis, 
            isRunning, 
            startTime, 
            pausedTime,
            finalTodayCompletedSeconds,
            currentDate
        )
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
            putInt(KEY_TODAY_COMPLETED_SECONDS, state.todayCompletedSeconds)
            putString(KEY_LAST_DATE, state.lastDate)
            apply()
        }
        _countdownState.value = state
        Log.d("CountdownRepository", "Saved state: total=${state.totalSeconds}, current=${state.currentMillis}, running=${state.isRunning}, todayCompleted=${state.todayCompletedSeconds}")
    }

    /**
     * 更新状态
     */
    fun updateState(update: (CountdownState) -> CountdownState) {
        val newState = update(_countdownState.value)
        saveState(newState)
    }

    /**
     * 添加今日完成的倒计时时长
     */
    fun addCompletedTime(completedSeconds: Int) {
        updateState { state ->
            state.copy(
                todayCompletedSeconds = state.todayCompletedSeconds + completedSeconds,
                lastDate = getCurrentDate()
            )
        }
    }

    /**
     * 获取当前日期字符串
     */
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
} 