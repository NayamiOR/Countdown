package com.example.countdown.data

/**
 * 倒计时状态数据类
 */
data class CountdownState(
    val totalSeconds: Int = 60,
    val currentMillis: Long = 0L,
    val isRunning: Boolean = false,
    val startTime: Long = 0L,
    val pausedTime: Long = 0L
) 