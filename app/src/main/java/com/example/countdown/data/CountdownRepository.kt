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
        private const val KEY_LAST_CLEAR_TIME = "last_clear_time"
        private const val KEY_TODAY_TIME_OFFSET = "today_time_offset"
        private const val KEY_AUTO_PAUSED = "auto_paused"
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
        val lastClearTime = prefs.getLong(KEY_LAST_CLEAR_TIME, 0L)
        val todayTimeOffset = prefs.getInt(KEY_TODAY_TIME_OFFSET, 0)
        val autoPaused = prefs.getBoolean(KEY_AUTO_PAUSED, false)

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
            currentDate,
            lastClearTime,
            todayTimeOffset,
            autoPaused
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
            putLong(KEY_LAST_CLEAR_TIME, state.lastClearTime)
            putInt(KEY_TODAY_TIME_OFFSET, state.todayTimeOffset)
            putBoolean(KEY_AUTO_PAUSED, state.autoPaused)
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

    /**
     * 清零今日累计时长
     */
    fun clearTodayCompletedTime() {
        val oldState = _countdownState.value
        Log.d("CountdownRepository", "Before clear - todayCompletedSeconds: ${oldState.todayCompletedSeconds}")
        
        // 计算当前实际累计的总时间（包括正在进行的）
        val currentTotalElapsed = if (oldState.isRunning || oldState.pausedTime > 0) {
            val totalMillisForCurrentSession = oldState.totalSeconds * 1000L
            val elapsedInCurrentSession = ((totalMillisForCurrentSession - oldState.currentMillis) / 1000).toInt()
            oldState.todayCompletedSeconds + elapsedInCurrentSession
        } else {
            oldState.todayCompletedSeconds
        }
        
        updateState { state ->
            state.copy(
                lastDate = getCurrentDate(),
                lastClearTime = System.currentTimeMillis(),
                // 设置偏移量，使当前累计时间在UI显示为0
                todayTimeOffset = currentTotalElapsed
            )
        }
        
        val newState = _countdownState.value
        Log.d("CountdownRepository", "After clear - set offset to: ${newState.todayTimeOffset}")
        Log.d("CountdownRepository", "Cleared today's completed time at ${newState.lastClearTime}")
    }
} 