package com.example.countdown.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.countdown.ui.components.CountdownProgress
import com.example.countdown.ui.components.TimeSettingDialog
import com.example.countdown.utils.TimeUtils
import com.example.countdown.viewmodel.CountdownViewModel

@Composable
fun CountdownScreen(
    viewModel: CountdownViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimeSettingDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题
        Text(
            text = "倒计时器",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 倒计时进度条
        CountdownProgress(
            progress = uiState.progress,
            formattedTime = uiState.formattedTime
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 控制按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 开始/暂停按钮
            Button(
                onClick = { viewModel.toggleCountdown() },
                modifier = Modifier.weight(1f),
                enabled = uiState.currentMillis > 0
            ) {
                Text(if (uiState.isRunning) "暂停" else "开始")
            }

            // 设为最大时间按钮
            Button(
                onClick = { viewModel.setCurrentAsMax() },
                modifier = Modifier.weight(1f),
                enabled = uiState.isRunning && uiState.currentMillis > 0
            ) {
                Text("剩余倒计时")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 设置时间按钮
            OutlinedButton(
                onClick = { showTimeSettingDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("设置时间")
            }

            // 重置按钮
            OutlinedButton(
                onClick = { viewModel.resetCountdown() },
                modifier = Modifier.weight(1f)
            ) {
                Text("重置")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 显示总时间
        Text(
            text = "总时间: ${uiState.formattedTotalTime}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // 时间设置对话框
    if (showTimeSettingDialog) {
        val (minutes, seconds) = TimeUtils.fromTotalSeconds(uiState.totalSeconds)
        TimeSettingDialog(
            initialMinutes = minutes,
            initialSeconds = seconds,
            onDismiss = { showTimeSettingDialog = false },
            onConfirm = { newMinutes, newSeconds ->
                viewModel.setNewTotalTime(newMinutes, newSeconds)
            }
        )
    }
} 