package com.example.countdown.data

/**
 * 倒计时状态数据类
 */
data class CountdownState(
    val totalSeconds: Int = 60,
    val currentMillis: Long = 0L,
    val isRunning: Boolean = false,
    val startTime: Long = 0L,
    val pausedTime: Long = 0L,
    val todayCompletedSeconds: Int = 0, // 今日完成的倒计时总秒数
    val lastDate: String = "", // 最后更新的日期（YYYY-MM-DD格式）
    val lastClearTime: Long = 0L, // 最后一次清零今日累计的时间戳
    val todayTimeOffset: Int = 0, // 今日累计时间的偏移量（用于清零后的计算）
    val autoPaused: Boolean = false // 是否因为应用进入后台而被自动暂停
) 