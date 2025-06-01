package com.example.countdown.utils

/**
 * 时间工具类
 */
object TimeUtils {
    /**
     * 将秒数格式化为 MM:SS 格式
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * 计算进度值
     */
    fun calculateProgress(currentMillis: Long, totalMillis: Long): Float {
        return if (totalMillis > 0) currentMillis.toFloat() / totalMillis.toFloat() else 0f
    }

    /**
     * 将分钟和秒转换为总秒数
     */
    fun toTotalSeconds(minutes: Int, seconds: Int): Int {
        return minutes * 60 + seconds
    }

    /**
     * 将总秒数转换为分钟和秒
     */
    fun fromTotalSeconds(totalSeconds: Int): Pair<Int, Int> {
        return Pair(totalSeconds / 60, totalSeconds % 60)
    }
} 