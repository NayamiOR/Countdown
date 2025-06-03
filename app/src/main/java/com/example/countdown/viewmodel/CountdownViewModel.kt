package com.example.countdown.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.countdown.data.CountdownRepository
import com.example.countdown.data.CountdownState
import com.example.countdown.utils.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel工厂类
 */
class CountdownViewModelFactory(private val repository: CountdownRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountdownViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CountdownViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * 倒计时视图模型
 * 处理倒计时的业务逻辑
 */
class CountdownViewModel(private val repository: CountdownRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CountdownUiState())
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        // 初始化时从仓库加载状态
        viewModelScope.launch {
            repository.countdownState.collect { state ->
                updateUiState(state)
            }
        }
    }

    /**
     * 更新UI状态
     */
    private fun updateUiState(state: CountdownState) {
        val currentSeconds = (state.currentMillis / 1000).toInt()
        val totalMillis = state.totalSeconds * 1000L
        val progress = TimeUtils.calculateProgress(state.currentMillis, totalMillis)

        // 计算今日实时累计时间：已完成的时间 + 当前正在进行的倒计时时间
        val currentElapsedSeconds = if (state.isRunning || state.pausedTime > 0) {
            // 如果正在运行或者暂停中，计算已经消耗的时间
            val totalMillisForCurrentSession = state.totalSeconds * 1000L
            val elapsedInCurrentSession =
                ((totalMillisForCurrentSession - state.currentMillis) / 1000).toInt()
            
            // 总累计时间 = 已完成时间 + 当前进行时间 - 偏移量
            val totalElapsed = state.todayCompletedSeconds + elapsedInCurrentSession - state.todayTimeOffset
            
            android.util.Log.d("CountdownViewModel", 
                "updateUiState: todayCompleted=${state.todayCompletedSeconds}, " +
                "elapsedInSession=$elapsedInCurrentSession, " +
                "offset=${state.todayTimeOffset}, " +
                "totalElapsed=$totalElapsed")
            
            kotlin.math.max(0, totalElapsed) // 确保不小于0
        } else {
            // 如果没有运行，只显示已完成的时间减去偏移量
            val totalElapsed = state.todayCompletedSeconds - state.todayTimeOffset
            android.util.Log.d("CountdownViewModel", 
                "updateUiState: not running, todayCompleted=${state.todayCompletedSeconds}, " +
                "offset=${state.todayTimeOffset}, totalElapsed=$totalElapsed")
            kotlin.math.max(0, totalElapsed) // 确保不小于0
        }

        // 决定是否显示时间选择界面
        val shouldShowTimePicker = when {
            state.isRunning -> false  // 正在运行时显示计时界面
            state.pausedTime > 0 -> false  // 暂停时也显示计时界面
            state.currentMillis <= 0 -> true  // 倒计时结束时显示时间选择界面
            else -> true  // 其他情况（如重置后）显示时间选择界面
        }

        _uiState.update { currentState ->
            currentState.copy(
                totalSeconds = state.totalSeconds,
                currentSeconds = currentSeconds,
                currentMillis = state.currentMillis,
                isRunning = state.isRunning,
                progress = progress,
                formattedTime = TimeUtils.formatTime(currentSeconds),
                formattedTotalTime = TimeUtils.formatTime(currentElapsedSeconds),
                shouldShowTimePicker = shouldShowTimePicker,
                wasAutoPaused = state.autoPaused
            )
        }
    }

    /**
     * 开始/暂停倒计时
     */
    fun toggleCountdown() {
        val currentState = repository.countdownState.value
        if (currentState.currentMillis <= 0) return

        if (currentState.isRunning) {
            // 注释给AI看的
            // 暂停时，不要将已进行的时间添加到今日累计中！

            repository.updateState { state ->
                state.copy(
                    isRunning = false,
                    pausedTime = state.totalSeconds * 1000L - state.currentMillis,
                    startTime = 0L,
                    autoPaused = false // 手动暂停，清除自动暂停标志
                )
            }
            countdownJob?.cancel()
        } else {
            // 开始
            repository.updateState { state ->
                state.copy(
                    isRunning = true,
                    startTime = 0L,
                    autoPaused = false // 手动开始，清除自动暂停标志
                )
            }
            startCountdown()
        }
    }

    /**
     * 开始倒计时
     */
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (repository.countdownState.value.isRunning &&
                repository.countdownState.value.currentMillis > 0
            ) {
                val currentTime = System.currentTimeMillis()
                val state = repository.countdownState.value

                if (state.startTime == 0L) {
                    repository.updateState { it.copy(startTime = currentTime - it.pausedTime) }
                    continue
                }

                val elapsed = currentTime - state.startTime
                val remaining = state.totalSeconds * 1000L - elapsed

                if (remaining <= 0) {
                    // 倒计时结束，保存剩余的最后一点时间
                    val finalElapsed =
                        ((state.totalSeconds * 1000L - state.currentMillis) / 1000).toInt()
                    if (finalElapsed > 0) {
                        repository.addCompletedTime(finalElapsed)
                    }

                    repository.updateState {
                        it.copy(
                            currentMillis = 0L,
                            isRunning = false,
                            startTime = 0L,
                            pausedTime = 0L
                        )
                    }
                    break
                } else {
                    repository.updateState { it.copy(currentMillis = remaining) }
                }

                delay(50) // 每50毫秒更新一次
            }
        }
    }

    /**
     * 重置倒计时
     */
    fun resetCountdown() {
        countdownJob?.cancel()

        val currentState = repository.countdownState.value

        // 如果有已进行的倒计时时间，先保存到今日累计中
        if (currentState.isRunning || currentState.pausedTime > 0) {
            val totalMillisForCurrentSession = currentState.totalSeconds * 1000L
            val elapsedInCurrentSession =
                ((totalMillisForCurrentSession - currentState.currentMillis) / 1000).toInt()

            if (elapsedInCurrentSession > 0) {
                repository.addCompletedTime(elapsedInCurrentSession)
            }
        }

        repository.updateState { state ->
            state.copy(
                currentMillis = state.totalSeconds * 1000L,
                isRunning = false,
                startTime = 0L,
                pausedTime = 0L
            )
        }
    }

    /**
     * 设置新的总时间
     */
    fun setNewTotalTime(hours: Int, minutes: Int, seconds: Int) {
        val newTotalSeconds = TimeUtils.toTotalSeconds(hours, minutes, seconds)
        if (newTotalSeconds <= 0) return

        countdownJob?.cancel()

        val currentState = repository.countdownState.value

        // 如果有已进行的倒计时时间，先保存到今日累计中
        if (currentState.isRunning || currentState.pausedTime > 0) {
            val totalMillisForCurrentSession = currentState.totalSeconds * 1000L
            val elapsedInCurrentSession =
                ((totalMillisForCurrentSession - currentState.currentMillis) / 1000).toInt()

            if (elapsedInCurrentSession > 0) {
                repository.addCompletedTime(elapsedInCurrentSession)
            }
        }

        repository.updateState { state ->
            state.copy(
                totalSeconds = newTotalSeconds,
                currentMillis = newTotalSeconds * 1000L,
                isRunning = false,
                startTime = 0L,
                pausedTime = 0L
            )
        }
    }

    /**
     * 将当前剩余时间设为新的最大时间
     */
    fun setCurrentAsMax() {
        val state = repository.countdownState.value
        if (!state.isRunning || state.currentMillis <= 0) return

        val newMaxSeconds = (state.currentMillis / 1000).toInt()
        repository.updateState { currentState ->
            currentState.copy(
                totalSeconds = newMaxSeconds,
                currentMillis = newMaxSeconds * 1000L,
                startTime = 0L,
                pausedTime = 0L
            )
        }
    }

    /**
     * 清零今日累计时长
     */
    fun clearTodayTotal() {
        // 只清零累计时长，不影响当前倒计时状态
        repository.clearTodayCompletedTime()
    }

    /**
     * 保存当前状态
     */
    fun saveCurrentState() {
        val currentState = repository.countdownState.value
        // 如果有正在进行的倒计时，先计算并保存已完成的时间
        if (currentState.isRunning && currentState.startTime > 0) {
            val elapsed = System.currentTimeMillis() - currentState.startTime
            val remaining = currentState.totalSeconds * 1000L - elapsed
            
            if (remaining > 0) {
                repository.updateState { state ->
                    state.copy(currentMillis = remaining)
                }
            }
        }
        android.util.Log.d("CountdownViewModel", "Current state saved")
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

/**
 * UI状态数据类
 */
data class CountdownUiState(
    val totalSeconds: Int = 60,
    val currentSeconds: Int = 60,
    val currentMillis: Long = 60000L,
    val isRunning: Boolean = false,
    val progress: Float = 1f,
    val formattedTime: String = "01:00",
    val formattedTotalTime: String = "00:00",
    val shouldShowTimePicker: Boolean = true, // 是否显示时间选择界面
    val wasAutoPaused: Boolean = false // 是否因应用后台而被自动暂停
) 