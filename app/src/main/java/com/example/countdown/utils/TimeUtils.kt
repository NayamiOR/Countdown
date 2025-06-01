package com.example.countdown.utils

/**
 * 时间工具类
 */
object TimeUtils {
    /**
     * 将秒数格式化为 HH:MM:SS 格式
     */
    fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    /**
     * 计算进度值
     */
    fun calculateProgress(currentMillis: Long, totalMillis: Long): Float {
        return if (totalMillis > 0) currentMillis.toFloat() / totalMillis.toFloat() else 0f
    }

    /**
     * 将小时、分钟和秒转换为总秒数
     */
    fun toTotalSeconds(hours: Int, minutes: Int, seconds: Int): Int {
        return hours * 3600 + minutes * 60 + seconds
    }

    /**
     * 将总秒数转换为小时、分钟和秒
     */
    fun fromTotalSeconds(totalSeconds: Int): Triple<Int, Int, Int> {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return Triple(hours, minutes, seconds)
    }
} 